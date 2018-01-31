//
//  CryptographyUtil.swift
//  RDWHolder
//
//
//  
//

import UIKit

class CryptographyUtil: NSObject {
    
    
    public static func getBase64StringOfPublicKey(key: SecKey) -> String{
        var error:Unmanaged<CFError>?
        let cfData = SecKeyCopyExternalRepresentation(key, &error)
        let length = CFDataGetLength(cfData!)
        let encryptedUnsafePointers = CFDataGetBytePtr(cfData!)!
        let encryptedBytes = UnsafeBufferPointer(start: encryptedUnsafePointers, count: length)
        let data = Data(buffer: encryptedBytes)
        return data.base64EncodedString()
    }
    public static func getHexStringOfPublicKey(key : SecKey) -> String{
        var error:Unmanaged<CFError>?
        let cfData = SecKeyCopyExternalRepresentation(key, &error)
        let length = CFDataGetLength(cfData!)
        let encryptedUnsafePointers = CFDataGetBytePtr(cfData!)!
        let encryptedBytes = UnsafeBufferPointer(start: encryptedUnsafePointers, count: length)
        let data = Data(buffer: encryptedBytes)
        return Util.byteArrayToStringHex(bytes: [UInt8] (data))
    }
    
    public static func getPublicKeyAndPrivateKeyForRSA(withKeySize sizeInBits : Int = 2048) -> [SecKey]? {
        var publicKey: SecKey?
        var privateKey: SecKey?
        let parameters  = [kSecAttrKeyType as String: kSecAttrKeyTypeRSA, kSecAttrKeySizeInBits as String: sizeInBits] as CFDictionary
        let statusCode = SecKeyGeneratePair(parameters, &publicKey, &privateKey)
        if statusCode == noErr && publicKey != nil && privateKey != nil {
            Util.printValue("Key pair generated OK")
            return [publicKey!, privateKey!]
        } else {
            Util.printValue("Error generating key pair: \(statusCode)")
            return nil
        }
    }
    
    public static func encryptAndReturnAsCFData(publicKey : SecKey, bytesToBeEncrypted: [UInt8] ) -> CFData? {
        if (SecKeyIsAlgorithmSupported(publicKey, .encrypt, SecKeyAlgorithm.rsaEncryptionPKCS1)) {
            var errorEncrypt : UnsafeMutablePointer<Unmanaged<CFError>?>?
            let plainCFData = CFDataCreate(kCFAllocatorDefault, bytesToBeEncrypted, bytesToBeEncrypted.count)
            let ciperhedText = SecKeyCreateEncryptedData(publicKey, .rsaEncryptionPKCS1, plainCFData! , errorEncrypt)
            return ciperhedText
        }else{
            return nil
        }
    }
    
    public static func encryptAndReturnAsByteArray (publicKey : SecKey, bytesToBeEncrypted: [UInt8] ) -> [UInt8]? {
        let encryptedCFData = encryptAndReturnAsCFData(publicKey: publicKey, bytesToBeEncrypted: bytesToBeEncrypted)
        if (encryptedCFData != nil) {
            let length = CFDataGetLength(encryptedCFData!)
            let encryptedUnsafePointers = CFDataGetBytePtr(encryptedCFData!)!
            let encryptedBytes = UnsafeBufferPointer(start: encryptedUnsafePointers, count: length)
            let data = Data(buffer: encryptedBytes)
            return [UInt8] (data)
        }else{
            return nil
        }
    }
    
    public static func decryptAndReturnAsCFData(privateKey : SecKey, ciperedText : CFData) -> CFData? {
        var errorDecrypt : UnsafeMutablePointer<Unmanaged<CFError>?>?
        if (SecKeyIsAlgorithmSupported(privateKey, SecKeyOperationType.decrypt, SecKeyAlgorithm.rsaEncryptionPKCS1) ) {
            let decryptedData = SecKeyCreateDecryptedData(privateKey, SecKeyAlgorithm.rsaEncryptionPKCS1, ciperedText , errorDecrypt)
            return decryptedData
        }else{
            return nil
        }
    }
    public static func decryptAndReturnAsByteArray (privateKey : SecKey, ciperedText : CFData) -> [UInt8]? {
        let decryptedData = decryptAndReturnAsCFData(privateKey: privateKey, ciperedText: ciperedText)
        if (decryptedData != nil) {
            let length = CFDataGetLength(decryptedData!)
            let decryptedUnsafePointers = CFDataGetBytePtr(decryptedData!)!
            let decryptedBytes = UnsafeBufferPointer(start: decryptedUnsafePointers, count: length)
            let data = Data(buffer: decryptedBytes)
            return [UInt8] (data)
        }else{
            return nil
        }
    }
    
    private static func getKeyFromStringValue(value : [UInt8]) -> SecKey?{
        var error : UnsafeMutablePointer<Unmanaged<CFError>?>?
        
        
        
        
        let parameters  = [kSecAttrKeyType as String : kSecAttrKeyTypeRSA,kSecAttrKeyClass as String: kSecAttrKeyClassPrivate, kSecAttrKeySizeInBits as String: 1024 ] as CFDictionary
        let keyCFData = CFDataCreate(kCFAllocatorDefault, value, value.count)
        if (keyCFData == nil) {
            return nil
        }
        
        
        let key = SecKeyCreateWithData(keyCFData!, parameters, error)
        if (error != nil) {
            Util.printValue("\(error!)")
        }
        return key
    }
    public static func importPrivateKey(privateKeyString : String) -> SecKey? {
        do{
            let newPrivateData = try stripHeaderIfAny(keyData: Data(base64Encoded: privateKeyString )! )
            return getKeyFromStringValue(value: [UInt8] (newPrivateData))
        }catch {
            Util.printValue("\(error)")
            return nil
        }
    }
    public static func signData(hexStringKey : String, dataToBeSigned : [UInt8]) -> [UInt8]? {
        do{
            let newPrivateData = try stripHeaderIfAny(keyData: Data(base64Encoded: hexStringKey )! )
            let privateKey =  getKeyFromStringValue(value: [UInt8] (newPrivateData))
            return signAndReturnAsByteArray(privateKey: privateKey!, bytesToBeSigned: dataToBeSigned)
        }catch {
            Util.printValue("\(error)")
            return nil
        }
    }
    private static func signAndReturnAsCFData(privateKey : SecKey, bytesToBeSigned: [UInt8]) -> CFData? {
        if (SecKeyIsAlgorithmSupported(privateKey, .sign, .rsaSignatureRaw)) {
            var errorSign : UnsafeMutablePointer<Unmanaged<CFError>?>?
            let plainCFData = CFDataCreate(kCFAllocatorDefault, bytesToBeSigned, bytesToBeSigned.count)
            let signedText = SecKeyCreateSignature(privateKey, SecKeyAlgorithm.rsaSignatureRaw, plainCFData!, errorSign)
            return signedText!
        }else{
            return nil
        }
    }
    private static func signAndReturnAsByteArray(privateKey : SecKey, bytesToBeSigned: [UInt8] ) -> [UInt8]? {
        let signedCFData = signAndReturnAsCFData(privateKey: privateKey, bytesToBeSigned: bytesToBeSigned)
        if (signedCFData != nil) {
            let length = CFDataGetLength(signedCFData!)
            let signedUnsafePointers = CFDataGetBytePtr(signedCFData!)
            let signedBytes = UnsafeBufferPointer(start: signedUnsafePointers, count: length)
            let data = Data(buffer: signedBytes)
            return [UInt8] (data)
        }
        return nil
    }
    private static func stripHeaderIfAny(keyData: Data) throws -> Data {
        var bytes = [UInt8](keyData)
        
        var offset = 0
        guard bytes[offset] == 0x30 else {
            throw SecurityError.WrongKeyError( message : "number 1")
        }
        offset += 1
        
        if bytes[offset] > 0x80 {
            offset += Int(bytes[offset]) - 0x80
        }
        offset += 1
        
        guard bytes[offset] == 0x02 else {
            throw  SecurityError.WrongKeyError(message : "number 2")
        }
        offset += 3
        
        //without PKCS8 header
        if bytes[offset] == 0x02 {
            return keyData
        }
        
        let OID: [UInt8] = [0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86, 0x48, 0x86,
                            0xf7, 0x0d, 0x01, 0x01, 0x01, 0x05, 0x00]
        let slice: [UInt8] = Array(bytes[offset..<(offset + OID.count)])
        
        guard slice == OID else {
            throw  SecurityError.WrongKeyError(message : "number 3")
        }
        
        offset += OID.count
        guard bytes[offset] == 0x04 else {
            throw  SecurityError.WrongKeyError(message : "number 4")
        }
        
        offset += 1
        if bytes[offset] > 0x80 {
            offset += Int(bytes[offset]) - 0x80
        }
        offset += 1
        
        guard bytes[offset] == 0x30 else {
            throw  SecurityError.WrongKeyError(message : "number 5")
        }
        
        return keyData.subdata(in: offset ..< (offset + keyData.count - offset))
        
        //return keyData.subdataWithRange(NSRange(location: offset, length: keyData.length - offset))
    }
}



