//
//  SampleCertificateLoader.swift
//  RDWHolder
//
//
//  
//

import UIKit

import ObjectMapper

//This class is to load the demo certificate that is stored in the example_licence.json file
class DemoCertificateLoader: NSObject, CertificateLoader {
    
    static private let fileName = "license"
    static private let fileExtension = "json"
    
    public static func doesLicenseContainPrivateKey(string: String) -> Bool {
        let data = string.data(using: .utf8)
        do{
        let jsonData = try JSONSerialization.jsonObject(with: data! , options: JSONSerialization.ReadingOptions.allowFragments) as! [String: Any]
            let privateKey = jsonData["aaprivateKey"] as? String
            Util.printValue("privateKey = \(privateKey)")
            if (privateKey == nil) {
                return false
            }
            return true
        }catch {
            print("Error while serializing the license : \(error)")
            return false
        }
    }
    
    func loadCertificate(certificate : String) -> Certificate? {
        let data = certificate.data(using: .utf8)
        do{
            let jsonData = try JSONSerialization.jsonObject(with: data! , options: JSONSerialization.ReadingOptions.allowFragments) as! [String: Any]
            let privateKey = jsonData["aaprivateKey"] as? String
            if (privateKey == nil) {
                return nil
            }
            var dataGroups = [String: DataGroup]()
            let ef = jsonData["ef"]
            let dataGroupsJson : [temporaryDataGroup] = Mapper<temporaryDataGroup>().mapArray(JSONObject: ef)!
            for dataGroup in dataGroupsJson {
                let idString = dataGroup.id!
                let id = Util.stringHexToByteArray(hexString: idString)!
                let name = dataGroup.name!
                let valueBase64 = dataGroup.value!
                let value = [UInt8] (Data(base64Encoded: valueBase64)!)
                let oneDataGroup = DataGroup(idString: idString, id: id, name: name, value: value)
                Util.printValue ("Found data group with id=\(idString)")
                dataGroups[idString.lowercased()] = oneDataGroup
            }
            do{
                return try Certificate(privateKey: privateKey!, dataGroups : dataGroups)
            } catch let error as ParsingError {
                print ("wrong private key")
                print (error)
                return nil
            }
            
        }catch {
            print(error.localizedDescription)
            return nil
        }
    }
    
    static func loadExampleCertificate() -> String{
        let data = Util.loadFile(fileName: DemoCertificateLoader.fileName, withExtension: DemoCertificateLoader.fileExtension)
        return NSString(data: data as Data, encoding: String.Encoding.utf8.rawValue)! as String
    }
    
    static func join(downloadedLicense : String, existingLicese : String) -> String? {
        
        do{
            let dataDownloadedLicense = downloadedLicense.data(using: .utf8)
            let jsonDataDownloadedLicense = try JSONSerialization.jsonObject(with: dataDownloadedLicense! , options: JSONSerialization.ReadingOptions.allowFragments) as! [String: Any]
            let ef = jsonDataDownloadedLicense["ef"]
            
            let dataExistingLicese = existingLicese.data(using: .utf8)
            let jsonDataExistingLicense = try JSONSerialization.jsonObject(with: dataExistingLicese!, options: JSONSerialization.ReadingOptions.allowFragments) as! [String: Any]
            let aaprivateKey = jsonDataExistingLicense["aaprivateKey"] as! String
            
            var results : [String : Any] = [:]
            results["aaprivateKey"] = aaprivateKey  
            results["ef"] = ef
            
            let finalData = try JSONSerialization.data(withJSONObject: results, options: .prettyPrinted)
            return String(data: finalData, encoding: .utf8)
            
        }catch {
            print("\(error)")
            return nil
        }
        
    }
    
}

class temporaryDataGroup : Mappable {
    var id : String?
    var name: String?
    var value : String?
    
    required init?(map: Map) {
        
    }
    func mapping(map: Map) {
        self.id <- map["id"]
        self.name  <- map["name"]
        self.value <- map["value"]
    }
}
