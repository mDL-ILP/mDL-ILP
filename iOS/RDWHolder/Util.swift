//
//  Util.swift
//  RDWHolder
//
//
//  
//

import UIKit




class Util: NSObject {
    public static func printValue(_ string : String) {
        if (ConfigurationManager.SHOW_PRINT == "true") {
            print(string)
        }
    }
    public static func loadFile(fileName :String, withExtension extensionName: String) -> NSData {
        let path = Bundle.main.path(forResource: fileName, ofType: extensionName)!
        let data = NSData(contentsOfFile: path)!
        return data
    }
    public static func concatenateArrays<T>(array1 : [T], array2 : [T])  -> [T]  {
        var result  = [T]()
        array1.forEach{t in  result.append(t)}
        array2.forEach{t in result.append(t)}
        return result
    }
    public static func stringHexToByteArray(hexString : String) -> [UInt8]?{
        let length = hexString.characters.count
        var bytes = [UInt8]()
        bytes.reserveCapacity(length/2)
        var index = hexString.startIndex
        for _ in 0..<length/2 {
            let nextIndex = hexString.index(index, offsetBy: 2)
            let range = index ..< nextIndex
            let subString = hexString.substring(with: range)
            let b = UInt8(subString, radix : 16)
            if b != nil{
                bytes.append(b!)
            }else{
                return nil
            }
            index = nextIndex
        }
        return bytes
    }
    public static func byteArrayToStringHex (bytes : [UInt8]) -> String{
        let data = Data(bytes: bytes)
        return data.map { String(format: "%02hhx", $0) }.joined()
    }
    
    public static func isBitOne(atIndex index : Int, ofByte byte: UInt8) -> Bool
    {
        let helperByte = formByteWithAllZerosExcepOneBitHasOne(atIndex: index)
        let result = helperByte! & byte
        switch result {
        case UInt8(0x00):
            return false
        case helperByte!:
            return true
        default:
            return false
        }
    }
    private static func formByteWithAllZerosExcepOneBitHasOne(atIndex index: Int) -> UInt8? {
        var byteStringBinary = ""
        for i in 1...8 {
            if (i == index) {
                byteStringBinary = "1"+byteStringBinary
            }else{
                byteStringBinary = "0"+byteStringBinary
            }
        }
        return UInt8(byteStringBinary, radix : 2)
    }
    public static func byteArrayToInt(bytes : [UInt8]) -> Int {
       return Int( byteArrayToStringHex(bytes: bytes), radix: 16)!
    }
    public static func generateRandomBytesArray(withSize size: Int) -> [UInt8]{
        var bytes = [UInt8]()
        for i in 0 ..< size {
           let randomNumber = Int32(arc4random_uniform(255))
            
            bytes.append(UInt8(randomNumber))
        }
        
        return bytes
    }
    public static func hashBytes(bytes : [UInt8])  -> [UInt8]{
        let data = Data(bytes: bytes)
        var hash = [UInt8](repeating: 0,  count: Int(CC_SHA256_DIGEST_LENGTH))
        data.withUnsafeBytes {
            _ = CC_SHA256($0, CC_LONG(data.count), &hash)
        }
        return [UInt8] (Data(bytes: hash))
        
    }
    
    public static func bcdToDateString(bcd: Data) -> String {
        let bcdConverted = byteArrayToStringHex(bytes: Array(bcd))
        
        let year = bcdConverted.substring(from: String.Index(encodedOffset: 4))
        let month = bcdConverted
            .substring(from: String.Index(encodedOffset: 2))
            .substring(to: String.Index(encodedOffset: 2))
        let day = bcdConverted.substring(to: String.Index(encodedOffset: 2))

        return year + "-" + month + "-" + day
    }
   
}
