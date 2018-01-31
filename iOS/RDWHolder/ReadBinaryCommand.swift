//
//  ReadBinaryCommand.swift
//  RDWHolder
//
//
//  
//

import UIKit

class ReadBinaryCommand: BaseCommand {
    //Because maximum MTU is 512 for the periperal and 3 bytes reserved for the protocol
    //and the last 2 bytes resereved
    let size = 505
    static var currentFile : String?
    
     init(cLA: UInt8, iNS: UInt8, p1: UInt8, p2: UInt8, remainingBytes: [UInt8]) {
        super.init(cLA: cLA, iNS: iNS, p1: p1, p2: p2)
        self.le = [remainingBytes[0]]
        printCommand()
    }
    private func getBytesFromDataGroup( fileIdentifier: String, certificate : Certificate, offsetValue : Int? = nil) throws -> [UInt8]{
        
        var offset = 0
        if (offsetValue == nil) {
            offset = Int(p2)
        }else{
            offset = offsetValue!
            Util.printValue("offset = \(offset)")
            Util.printValue("file = \(ReadBinaryCommand.currentFile!)")
        }
        
        let dataGroup = certificate.dataGroups[fileIdentifier.lowercased()]
        if (dataGroup == nil) {
            throw CommandError.FileIdentifierNotFound(message: "The Read Binary command asks for data from none existance file. Received file identifier:\(fileIdentifier)")
        }else{
            if (le?.count == 1 && le![0] == UInt8(0x00)) {
                var finalIndexToRead = 0
                if (dataGroup!.value.count >= size){
                    if ((size + offset) >= dataGroup!.value.count) {
                        finalIndexToRead = dataGroup!.value.count
                    }else{
                        finalIndexToRead = size + offset
                    }
                    ReadBinaryCommand.currentFile = fileIdentifier
                    return Util.concatenateArrays(array1: [UInt8](dataGroup!.value[offset ..< finalIndexToRead]), array2: Util.stringHexToByteArray(hexString: "9000")!)
                }else{
                    ReadBinaryCommand.currentFile = fileIdentifier
                    return Util.concatenateArrays(array1: [UInt8](dataGroup!.value[offset ..<  dataGroup!.value.count]), array2: Util.stringHexToByteArray(hexString: "9000")!)
                }
            }else{
                if (offset > dataGroup!.value.count ) {
                    ReadBinaryCommand.currentFile = fileIdentifier
                    return [UInt8] (Util.stringHexToByteArray(hexString: "6B00")!)
                }
                ReadBinaryCommand.currentFile = fileIdentifier
                return Util.concatenateArrays(array1: [UInt8](dataGroup!.value[offset ..< Util.byteArrayToInt(bytes: le!)]), array2: Util.stringHexToByteArray(hexString: "9000")!)
            }
        }
    }
    override func getRespons(certificate : Certificate) throws -> [UInt8] {
        if (!Util.isBitOne(atIndex: 1, ofByte: iNS) && Util.isBitOne(atIndex: 8, ofByte: p1)
       //     && !Util.isBitOne(atIndex: 7, ofByte: p1) && !Util.isBitOne(atIndex: 6, ofByte: p1)
            )
        {
            let helperByte = UInt8(0x1F)
            let ef = helperByte & p1
            let fileIdentifier = Util.byteArrayToStringHex(bytes: [UInt8(0x00), ef])
            return try getBytesFromDataGroup(fileIdentifier: fileIdentifier, certificate:  certificate)
        }
        
        if (!Util.isBitOne(atIndex: 1, ofByte: iNS) && !Util.isBitOne(atIndex: 8, ofByte: p1)) {
            return try getBytesFromDataGroup(fileIdentifier: ReadBinaryCommand.currentFile!, certificate: certificate, offsetValue : Util.byteArrayToInt(bytes: [p1, p2]))
        }
        if (Util.isBitOne(atIndex: 1, ofByte: iNS)) {
            //Checking if the first 11 bits of P1-P2 are zeros:
            //We will check that the 8 bits of P1 are zeros, and the LAST 3 bits of P2 are zeros:
            if (p1 == UInt8(0x00) && !Util.isBitOne(atIndex: 8, ofByte: p2) && !Util.isBitOne(atIndex: 7, ofByte: p2) && Util.isBitOne(atIndex: 6, ofByte: p2)) {
                //Checking if bits 1 to 5 of p2 are not equal
                //We multiply p2 with 0x00 byte, if the result is either:
                //0x00 (00000000) that means the first 5 bits are zeros, or
                // 0x1F (00011111) that means the first 5 bits are ones
                let helperByte : UInt8 = 0x00
                if (helperByte == UInt8(0x00) || helperByte == UInt8(0x1F) ) {
                    let fileIdentifier = Util.byteArrayToStringHex(bytes: [p1, p2])
                    return try getBytesFromDataGroup(fileIdentifier: fileIdentifier, certificate: certificate)
                }else{
                    let helperByte = UInt8(0x1F)
                    let ef = helperByte & p2
                    let fileIdentifier = Util.byteArrayToStringHex(bytes: [UInt8(0x00), ef])
                    return try getBytesFromDataGroup(fileIdentifier: fileIdentifier, certificate: certificate)
                }
            }
        }
        throw CommandError.NoCaseMatched(message : "We parsed the incomming command, and no matching case found!")
    }
}
