//
//  InternalAuthenticateCommand.swift
//  RDWHolder
//
//
//  
//

import UIKit

class InternalAuthenticateCommand: BaseCommand {
    init(cLA: UInt8, iNS: UInt8, p1: UInt8, p2: UInt8, remainingBytes : [UInt8]) {
        //p1 references the algorithm to use: either a cryptographic one or a biometric one:
        super.init(cLA: cLA, iNS: iNS, p1: p1, p2: p2)
        super.lc = remainingBytes[0]
        super.data = [UInt8](remainingBytes[1...Int(lc!)])
        super.le = [UInt8] (remainingBytes[(1+Int(lc!)) ..< remainingBytes.count])
        super.printCommand()
    }
    
    override func getRespons(certificate : Certificate) throws  -> [UInt8] {
        if (p1 == UInt8(0x00) && p2 == UInt8(0x00)) {
            /*let keys = CryptographyUtil.getPublicKeyAndPrivateKeyForRSA()
            let encryptedBytes = CryptographyUtil.encryptAndReturnAsByteArray(publicKey: keys![0], bytesToBeEncrypted: data!)*/
            let randomBytes = Util.generateRandomBytesArray(withSize: 127-34)
            Util.printValue("size of randomBytes = \(randomBytes.count)")
            let randomAndData = Util.concatenateArrays(array1: randomBytes, array2: data!)
            let hashedData = Util.hashBytes(bytes: randomAndData)
            Util.printValue("size of hashedData = \(hashedData.count)")
            Util.printValue("----")
            let first = Util.concatenateArrays(array1: Util.stringHexToByteArray(hexString: "6A")!, array2: randomBytes)
            let second = Util.concatenateArrays(array1: first, array2: hashedData)
            let finalDataToBeSigned = Util.concatenateArrays(array1: second, array2: Util.stringHexToByteArray(hexString: "34CC")!)
            
            Util.printValue("size of the finalDataToBeSigned =\(finalDataToBeSigned.count)")
            Util.printValue("finalDataToBeSigned=\(Util.byteArrayToStringHex(bytes: finalDataToBeSigned))")
            
            let encryptedBytes = CryptographyUtil.signData(hexStringKey: certificate.privateKeyString, dataToBeSigned: finalDataToBeSigned)
            
            Util.printValue("encryptedBytes=\(Util.byteArrayToStringHex(bytes: encryptedBytes!))")
            
            
            if (encryptedBytes != nil) {
                return Util.concatenateArrays(array1: encryptedBytes!, array2: [UInt8(0x90), UInt8(0x00)])
            }else{
                throw CommandError.EncryptionError
            }
        }
        throw CommandError.NoCaseMatched(message: "Internal Authenticate command no case matched")
    }
    
}
