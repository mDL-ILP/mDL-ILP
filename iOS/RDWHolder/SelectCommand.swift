//
//  SelectCommand.swift
//  RDWHolder
//
//
//  
//

import UIKit

class SelectCommand: BaseCommand {
    /*func getRespons() throws -> [UInt8] {
     
     }*/
    
    init(cLA: UInt8, iNS: UInt8, p1: UInt8, p2: UInt8, remainingBytes : [UInt8]) {
        super.init(cLA: cLA, iNS: iNS, p1: p1, p2: p2)
        super.lc = remainingBytes[0]
        super.data = [UInt8](remainingBytes[1...Int(lc!)])
        super.le = [UInt8] (remainingBytes[(1+Int(lc!)) ..< remainingBytes.count])
        super.printCommand()
    }
    
    
    override func getRespons(certificate : Certificate) throws  -> [UInt8] {
        if (p1 == UInt8(0x00)) {
         //   return error with 6900
            throw CommandError.Ask (message : "Declaration at page 60")
        }
        if (p1 == UInt8(0x04)) {
            return [UInt8]([0x90, 0x00])
        }
        throw CommandError.NoCaseMatched(message: "Select command no case matched")
    }
    
}

