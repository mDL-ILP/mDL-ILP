//
//  ViewController.swift
//  RDWHolder
//
//
//  
//

import UIKit
import CoreBluetooth

class QRCodeGeneratorViewController: UIViewController, CBCentralManagerDelegate, CBPeripheralDelegate, FileManagerProtocol, RequestTokenProtocol {
   
    var quCodeImage: CIImage!
    var type : String?
    var numberOfReads = 0
    
    var dataMinimizationParameter = "00";
    
    let serviceUUID = CBUUID(string: "00001234-0000-1000-8000-00805f9b34fb")
    let characteristicUUID = CBUUID(string: "00002315-0000-1000-8000-00805f9b34fb")
    
    var  cBCentralManager: CBCentralManager?
    var discoveredPeripheral: CBPeripheral?
    var discoveredCharacteristic : CBCharacteristic?
    var commandParser : CommandParser?
    
    var certificateLoader : CertificateLoader?
    var certificate : Certificate?
    var certificateString : String?
    
    
    func onSuccessfullyDeletedLicense() {
        //No need for it to have body
    }
    
    func onSuccessfullySavedLicense() {
        //No need for it to have body
    }
    
    func onError(message: String) {
        showAlertWithTitle(title: "Notice", message: message)
    }
    
    func onRequestTokenReceived(token: String) {
        Util.printValue(">>>>> Token from server: " + token)
        
        let hexData =
            "5F 29 50" + // interface independent profile
                "80 08 30 31 2E 30 31 2E 30 31" +
                "34 10 85 54 B5 33 4B 48 4A 6B AE 7D 40 34 B4 AC 97 E6" +
                "81 00" +
                "82 00" +
                "83 01" + dataMinimizationParameter +
                "67 26 06 08 04 00 7F 00 07 02 02 04 80 1A 37 64 6C 30 62 68 75 67 76 76 30 64 33 34 72 6A 6B 6E 69 6D 6E 74 61 39 65 61" +
                "5F 2D 02 65 6E" +
            "5F 29 42" + // Internet profile
                "80 08 30 31 2E 30 31 2E 30 31" +
                "34 10 92 E3 F6 6D C7 0F 42 3E 8D 11 C2 B0 65 8B 9A 1D" +
                "81 24";
        
        let data = hexStringToBytes(hexData + token.unicodeScalars.filter { $0.isASCII }.map { String(format: "%X", $0.value) }.joined())
        let b64data = data.base64EncodedString()
        
        Util.printValue(">>>>> B64: " + b64data)
        setQRCode(b64data)
    }
    
    @IBOutlet weak var ivQRCode: UIImageView!
    fileprivate func setQRCode(_ data: String) {
        let filter = CIFilter(name: "CIQRCodeGenerator")!
        filter.setValue(data.data(using: String.Encoding.ascii)!, forKey: "inputMessage")
        filter.setValue("Q", forKey: "inputCorrectionLevel")
        quCodeImage = filter.outputImage
        
        let scale = ivQRCode.frame.size.height / quCodeImage.extent.size.height
        let transformedImage = quCodeImage.applying(CGAffineTransform(scaleX: scale, y: scale))
        ivQRCode.image = UIImage(ciImage: transformedImage)
        certificateString = FileUtil.readLicense(delegate: self)
    }
    
    fileprivate func setUpBLE() {
        let hexData =
            "5F 29 50" + // interface independent profile
                "80 08 30 31 2E 30 31 2E 30 31" +
                "34 10 85 54 B5 33 4B 48 4A 6B AE 7D 40 34 B4 AC 97 E6" +
                "81 00" +
                "82 00" +
                "83 01" + dataMinimizationParameter +
                "67 26 06 08 04 00 7F 00 07 02 02 04 80 1A 37 64 6C 30 62 68 75 67 76 76 30 64 33 34 72 6A 6B 6E 69 6D 6E 74 61 39 65 61" +
                "5F 2D 02 65 6E" +
                "5F 29 46" + // BLE profile
                "80 08 30 31 2E 30 31 2E 30 31" +
                "34 10 81 26 C4 8F 50 D0 41 72 85 2E 47 04 20 91 DE 14" +
                "81 00" +
                "82 08 32 32 69 32 6D 31 73 32" +
                "83 07 55 6E 6B 6E 6F 77 6E" +
        "84 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 85 01 58";
        
        let data = hexStringToBytes(hexData)
        let b64data = data.base64EncodedString()
        setQRCode(b64data)
        startScanning()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //let data = ("BLE:"+randomString(length: 8)+";"+randomString(length: 27)+";"+type!).data(using: .isoLatin1)
        // HACK: should only be used in case of a real license
        // and only when we actually do a web transfer
        let message = RequestTokenMessage()
        message.deviceToken = UserDefaults.standard.value(forKey: SharedApplicationConstants.firebase_token) as? String
        message.permittedDatagroups = ["0001", "0006", "000A", "000B","000F","0010"]
        NetworkClient.requestToken(delegate: self, message: message)
        
        switch (self.type ?? "") {
        case "18":
            self.dataMinimizationParameter = "12"
        case "21":
            self.dataMinimizationParameter = "15"
        default:
            self.dataMinimizationParameter = "00"
        }
        
        print(self.dataMinimizationParameter)
        
        setUpBLE()
        
    }
    
    func hexStringToBytes(_ value: String) -> NSData {
        var hexString = value.replacingOccurrences(of: " ", with: "")
        
        let chars = hexString.cString(using: String.Encoding.utf8)!
        var i = 0
        
        
        let length = hexString.characters.count
        
        let data = NSMutableData(capacity: length/2)!
        var byteChars: [CChar] = [0, 0, 0]
        
        var wholeByte: CUnsignedLong = 0
        
        while i < length {
            byteChars[0] = chars[i]
            i+=1
            byteChars[1] = chars[i]
            i+=1
            wholeByte = strtoul(byteChars, nil, 16)
            data.append(&wholeByte, length: 1)
        }
        
        return data
    }
    
    private func startScanning() {
        //Starting up a Central manager
        cBCentralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        Util.printValue ("We are in centralManagerDidUpdateState")
        if (central.state == .poweredOn) {
            //Discovering Peripheral devices that are advertising
            cBCentralManager!.scanForPeripherals(withServices: [serviceUUID], options: nil)
        }else{
            showAlertWithTitle(title: "Error", message: "Connection is not in PoweredOn state")
        }
    }
    
    func randomString(length: Int) -> String {
        
        let letters : NSString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        let len = UInt32(letters.length)
        
        var randomString = ""
        
        for _ in 0 ..< length {
            let rand = arc4random_uniform(len)
            var nextChar = letters.character(at: Int(rand))
            randomString += NSString(characters: &nextChar, length: 1) as String
        }
        
        return randomString
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        Util.printValue ("We found a peripheral ...")
        //dump(advertisementData)
        self.discoveredPeripheral = peripheral
        Util.printValue("The maximut MUT size that the peripheral accepts is: \(self.discoveredPeripheral!.maximumWriteValueLength(for: .withResponse ))")
        //Stop scaning to save battery
        self.cBCentralManager!.stopScan()
        //Connecting to a a Peripheral device after we've discovered it

        cBCentralManager!.connect(self.discoveredPeripheral!, options: nil)
    }
    
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        Util.printValue ("You are now connected to the peripheral \(peripheral.name))")
        self.commandParser = CommandParser()
        self.certificateLoader = DemoCertificateLoader()
        

        self.certificate = certificateLoader!.loadCertificate(certificate: certificateString!)
        
        if (certificate == nil) {
            if (cBCentralManager != nil && self.discoveredCharacteristic != nil) {
                cBCentralManager!.cancelPeripheralConnection(self.discoveredPeripheral!)
            }
            showAlertWithTitle(title: "Error", message: "Couldn't load the certificate")
            return
        }
        peripheral.delegate = self
        self.discoveredPeripheral!.delegate = self
        //Discover the services of a Peripheral
        peripheral.discoverServices([serviceUUID])
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        Util.printValue ("We discovered the following services on the peripheral \(String(describing: peripheral.name)))")
        for service in peripheral.services! {
            Util.printValue("\(service)")
            //Discovering the characteristics of the service
            peripheral.discoverCharacteristics([characteristicUUID], for: service)
        }
    }
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        Util.printValue ("We discovered the following characteristics for the serivce \(service)")
        for cBCharacteristic in service.characteristics! {
            print ("\(cBCharacteristic)")
            //Subscribing to the Characteristic's value, which will call didUpdateNotificationStateFor
            //peripheral.setNotifyValue(true, for: cBCharacteristic)
            self.discoveredCharacteristic = cBCharacteristic
            
            
            read()
            
        }
    }
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        Util.printValue ("we are in didUpdateValueFor method")
        let value = characteristic.value
        Util.printValue ("we got this value")
        Util.printValue("\(String(describing: value))")
        if (error != nil) {
            print("There's an error!")
        }else{
            if (value!.isEmpty) //BLE wrapper last command {
            {
                write(usedCardSimulator: .Fake, command: nil)
            }else{
                do{
                    let command = try commandParser!.getCommand(bytes: [UInt8] (value!))
                    Util.printValue("number of reads = \(numberOfReads)")
                    write(usedCardSimulator: .Real, command: command!)
                }catch let error as ParsingError{
                    showAlertWithTitle(title: "Error", message: error.localizedDescription)
                }catch {
                    showAlertWithTitle(title: "Error", message: error.localizedDescription)
                }
            }
        }
    }
    
    private func read() {
        Util.printValue ("we are about to read")
        self.numberOfReads = self.numberOfReads + 1
        //Reading the value of the Characteristic, which will call didUpdateValueFor characteristic
        self.discoveredPeripheral!.readValue(for: self.discoveredCharacteristic!)
    }
    
    private func write(usedCardSimulator : UsedCardSimulator, command : Command?) {
        Util.printValue("we are about to write to the characteristic")
        if (usedCardSimulator == .Fake) {
            /*let source = FakeSimulator()
             print("number of reader minuse 1 = \(numberOfReads-1)")
             let data = Data(bytes : source.bytesValues[numberOfReads-1]!)*/
            let data = Data(bytes: [UInt8]())
            var m : CBMutableService
            self.discoveredPeripheral?.writeValue(data, for: self.discoveredCharacteristic!, type: .withResponse)
        }
        else{
            do{
                let bytes = try command!.getRespons(certificate: certificate!)
                let hexValue = Util.byteArrayToStringHex(bytes: bytes)
                Util.printValue("we will write: \(hexValue)")
                let data = Data(bytes: bytes)
                self.discoveredPeripheral?.writeValue(data, for: self.discoveredCharacteristic!, type: .withResponse)
            }catch {
                print(error)
            }
            
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        Util.printValue ("We are in didWriteValueFor characteristic")
        if( error != nil) {
            print ("Error when writing to the characteristic")
        }else{
            Util.printValue ("Writing done successfully")
            read()
        }
    }
    
    
    func showAlertWithTitle( title:String, message:String ) {
        
        let alertVC = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        let okAction = UIAlertAction(title: "Ok", style: .default, handler: nil)
        alertVC.addAction(okAction)
        
        DispatchQueue.main.async  { () -> Void in
            
            self.present(alertVC, animated: true, completion: nil)
            
        }
    }
    
}

enum UsedCardSimulator {
    case Fake
    case Real
}

