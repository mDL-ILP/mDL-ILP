//
//  CommandParser.swift
//  RDWHolder
//
//
//  
//

import UIKit

class CommandParser: NSObject {
    func getCommand (bytes : [UInt8]) throws -> Command?{
        try validateBytes(bytes: bytes)
        let cLA = bytes[0]
        let iNS = bytes[1]
        let p1 = bytes[2]
        let p2 = bytes[3]
        let bytesArraySize = bytes.count
        let remainingBytes =  [UInt8](bytes[4 ..< bytesArraySize])
        let hexIns = Util.byteArrayToStringHex(bytes: [iNS])
        switch (hexIns) {
        case "A4", "a4":
            Util.printValue("received Select command with the following load")
            Util.printValue("\(Util.byteArrayToStringHex(bytes: bytes))")
            return SelectCommand(cLA: cLA, iNS: iNS, p1: p1, p2: p2, remainingBytes: remainingBytes)
        case "b0", "B0":
            Util.printValue("received Read Binary command with the following load")
            Util.printValue("\(Util.byteArrayToStringHex(bytes: bytes))")
            return ReadBinaryCommand(cLA: cLA, iNS: iNS, p1: p1, p2: p2, remainingBytes: remainingBytes)
        case "88" :
            Util.printValue("received Internal Authenticate command with the following load")
            Util.printValue("\(Util.byteArrayToStringHex(bytes: bytes))")
            return InternalAuthenticateCommand(cLA: cLA, iNS: iNS, p1: p1, p2: p2, remainingBytes: remainingBytes)
        default:
            print ("New command \(hexIns)")
            return nil
        }
    }
    func validateBytes (bytes: [UInt8]) throws {
        if (bytes.count < 5) {
            throw ParsingError.invalidRequest(message: "Invalid number of bytes, we recived \(bytes.count) bytes, which is sless than 5")
        }
    }
}

