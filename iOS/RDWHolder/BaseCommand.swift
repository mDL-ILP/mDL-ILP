//
//  BaseCommand.swift
//  RDWHolder
//
//
//  
//

import UIKit

class BaseCommand: NSObject, Command {
    let cLA : UInt8
    let iNS : UInt8
    let p1 : UInt8
    let p2 : UInt8
    var le : [UInt8]?
    var lc : UInt8?
    var data : [UInt8]?
    func getRespons(certificate : Certificate) throws  -> [UInt8] {
        throw CommandError.NotImplementedCommand
    }
    
    init(cLA: UInt8, iNS: UInt8, p1: UInt8, p2: UInt8) {
        self.cLA = cLA
        self.iNS = iNS
        self.p1 = p1
        self.p2 = p2
    }
    func printCommand () {
        Util.printValue("CLA=\(Util.byteArrayToStringHex(bytes: [cLA]))")
        Util.printValue("INS=\(Util.byteArrayToStringHex(bytes: [iNS]))")
        Util.printValue("P1=\(Util.byteArrayToStringHex(bytes: [p1]))")
        Util.printValue("P2=\(Util.byteArrayToStringHex(bytes: [p2]))")
        if (lc != nil) {
            Util.printValue("Lc=\(Util.byteArrayToStringHex(bytes: [lc!]))")
        }
        if (data != nil) {
            Util.printValue("Data=\(Util.byteArrayToStringHex(bytes: data!))")
        }
        if (le != nil) {
            Util.printValue("Le=\(Util.byteArrayToStringHex(bytes: le!))")
        }
    }
}
