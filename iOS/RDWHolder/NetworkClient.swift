//
//  Register.swift
//  RDWHolder
//
//
//  
//

import UIKit
import Alamofire
import AlamofireObjectMapper
class NetworkClient: NSObject {
    
    
    private static var Manager : Alamofire.SessionManager = {
        // Create the server trust policies
        let serverTrustPolicies: [String: ServerTrustPolicy] = [
            "rdw-poc-mdl.westeurope.cloudapp.azure.com": .disableEvaluation
        ]
        // Create custom manager
        let configuration = URLSessionConfiguration.default
        configuration.httpAdditionalHeaders = Alamofire.SessionManager.defaultHTTPHeaders
        let man = Alamofire.SessionManager(
            configuration: URLSessionConfiguration.default,
            serverTrustPolicyManager: ServerTrustPolicyManager(policies: serverTrustPolicies)
        )
        return man
    }()
    
   /* static func transfer < T : TransferProtocol> (delete : T, message : TransferMessage) {
        
    }*/
    
    static func download<T : DownloadProtocol, E : FileManagerProtocol> (delegate : T, message : DownloadMessage, fileDelegate : E) {
        let bodyString = message.toJSONString()!
        print ("body download = \(bodyString)")
        let bodyData = bodyString.data(using: .utf8)!
        let url = NSURL(string: "\(ConfigurationManager.RDW_ENDPOINT_BASE)\(ConfigurationManager.RDW_DOWNLOAD)")! as URL
        Util.printValue("download url = \(url)")
        let headers : HTTPHeaders = ["content-type" : "application/json", "method": "POST"]
        Manager.upload(bodyData, to: url, method: .post, headers: headers).response {
            defaultDataResponse in
            let error = defaultDataResponse.error
            if (error != nil) {
                Util.printValue("Error when downloading the certificate")
                delegate.onError(message: error!.localizedDescription)
            }else{
                
                let response = defaultDataResponse.response
                let data = defaultDataResponse.data
                
                let status = defaultDataResponse.response?.statusCode
                if (status! >= 200 && status! < 300) {
                    var dowloadedLicenseString = String(bytes: [UInt8](data!), encoding: .utf8)
                    Util.printValue(dowloadedLicenseString!)
                    if (!DemoCertificateLoader.doesLicenseContainPrivateKey(string: dowloadedLicenseString!)) {
                        let existedLicenseString = FileUtil.readLicense(delegate: fileDelegate)
                        if (existedLicenseString != nil) {
                            dowloadedLicenseString = DemoCertificateLoader.join(downloadedLicense: dowloadedLicenseString!, existingLicese: existedLicenseString!)
                        }
                    }
                    
                    delegate.onSuccessfullDownload(certificate: dowloadedLicenseString! )
                }else{
                    Util.printValue("resposne code = \(status)")
                    if (data != nil) {
                        Util.printValue("response = \(String(bytes: [UInt8](data!), encoding: .utf8 ))")
                    }
                    delegate.onError(message : "Erro: response code = \(status)")
                }
                
            }
        }
    }
    
    static func requestTransferId<T: TransferRequesterProtocol>(delegate : T, message : RequestTransferMessage) {
        let bodyString = message.toJSONString()!
        print ("body request transfer id = \(bodyString)")
        let bodyData = bodyString.data(using: .utf8)!
        let url = NSURL(string: "\(ConfigurationManager.RDW_ENDPOINT_BASE)\(ConfigurationManager.RDW_REQUEST_TRANSFER_ID)")! as URL
        let headers : HTTPHeaders = ["content-type" : "application/json", "method": "POST"]
        Manager.upload(bodyData, to: url, method: .post, headers: headers).response {
            defaultDataResponse in
            let error = defaultDataResponse.error
            if (error != nil) {
                print("Error when requesting id")
                delegate.onError(message: error!.localizedDescription)
            }else{
                let data = defaultDataResponse.data
                if (data != nil) {
                    let stringUtf8Data = String(bytes: [UInt8](data!), encoding: .utf8 )
                    Util.printValue("response = \(stringUtf8Data)")
                }
                
                let status = defaultDataResponse.response?.statusCode
                if (status! >= 200 && status! < 300) {
                    do{
                        
                        
                        let jsonData = try JSONSerialization.jsonObject(with: data!, options: JSONSerialization.ReadingOptions.allowFragments ) as! [String: String]
                    delegate.onTransferRequestIdReceived(id : jsonData["id"]!)
                        
                    }catch {
                        print(error)
                    }
                }else{
                    print("resposne code = \(status)")
                    delegate.onError(message : "Erro: response code = \(status)")
                }
                
            }
        }
    }
    static func register<T : RegistrationProtocol>(delegate : T, message : RegistrationMessage) {
        let bodyString = message.toJSONString()!
        print("body = \(bodyString)")
        let bodyData = bodyString.data(using: .utf8)!
        let url = NSURL(string: "\(ConfigurationManager.RDW_ENDPOINT_BASE)\(ConfigurationManager.RDW_REGISTER)")! as URL
        let headers : HTTPHeaders = ["content-type" : "application/json", "method": "POST"]
        
        
        Manager.upload(bodyData, to: url, method: .post, headers: headers).response{ defaultDataResponse in
            let error = defaultDataResponse.error
            if (error != nil) {
                print("Error when registering")
                delegate.onError(message: error!.localizedDescription)
            }else{
                let response = defaultDataResponse.response
                let data = defaultDataResponse.data
                if (data != nil) {
                    print("response = \(String(bytes: [UInt8](data!), encoding: .utf8 ))")
                }
                let status = defaultDataResponse.response?.statusCode
                if (status! >= 200 && status! < 300) {
                    delegate.onSuccessfullyRegistered()
                }else{
                    print("resposne code = \(status)")
                    delegate.onError(message : "Erro: response code = \(status)")
                }
            }
        }
    }
    
    // Request online token:
    static func requestToken<T : RequestTokenProtocol>(delegate : T, message : RequestTokenMessage) {
        let bodyString = message.toJSONString()!
        print("body request token= \(bodyString)")
        let bodyData = bodyString.data(using: .utf8)!
        let url = NSURL(string: "\(ConfigurationManager.RDW_ENDPOINT_BASE)\(ConfigurationManager.RDW_REQUEST_TOKEN)")! as URL
        let headers : HTTPHeaders = ["content-type" : "application/json", "method": "POST"]
        
        Manager.upload(bodyData, to: url, method: .post, headers: headers).response {
            defaultDataResponse in
            let error = defaultDataResponse.error
            if (error != nil) {
                print("Error when requesting token")
                delegate.onError(message: "Internet connection not available; using Bluetooth Low Energy.")
            }else{
                let data = defaultDataResponse.data
                if (data != nil) {
                    let stringUtf8Data = String(bytes: [UInt8](data!), encoding: .utf8 )
                    Util.printValue("response = \(stringUtf8Data)")
                }
                
                let status = defaultDataResponse.response?.statusCode
                if (status! >= 200 && status! < 300) {
                    do{
                        
                        
                        let jsonData = try JSONSerialization.jsonObject(with: data!, options: JSONSerialization.ReadingOptions.allowFragments ) as! [String: String]
                        delegate.onRequestTokenReceived(token : jsonData["token"]!)
                        
                    }catch {
                        print(error)
                    }
                }else{
                    print("response code = \(status)")
                    delegate.onError(message : "Online token not available for this (example) license; using Bluetooth Low Energy.")
                }
                
            }
        }
    }
    
    static func permitTransfer<T : PermitTransferProtocol>(delegate : T, message : PermitTransferMessage) {
        let bodyString = message.toJSONString()!
        print("body request token= \(bodyString)")
        let bodyData = bodyString.data(using: .utf8)!
        let url = NSURL(string: "\(ConfigurationManager.RDW_ENDPOINT_BASE)\(ConfigurationManager.RDW_PERMIT_TRANSFER)")! as URL
        let headers : HTTPHeaders = ["content-type" : "application/json", "method": "POST"]
        
        Manager.upload(bodyData, to: url, method: .post, headers: headers).response {
            defaultDataResponse in
            let error = defaultDataResponse.error
            if (error != nil) {
                print("Error when requesting token")
                delegate.onError(message: "Could not connect to backend server; transfer not successful")
            }else{
                let data = defaultDataResponse.data
                if (data != nil) {
                    let stringUtf8Data = String(bytes: [UInt8](data!), encoding: .utf8 )
                    Util.printValue("response = \(stringUtf8Data)")
                }
                
                let status = defaultDataResponse.response?.statusCode
                if (status! >= 200 && status! < 300) {
                    do{
                        delegate.onSuccesfullyTransferred()
                    }catch {
                        print(error)
                    }
                }else{
                    print("response code = \(status)")
                    delegate.onError(message : "Could not connect to backend server; transfer not successful")
                }
                
            }
        }
    }
    
}


