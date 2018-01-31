//
//  Command.swift
//  RDWHolder
//
//
//  
//

import UIKit

protocol Command {
    func getRespons(certificate : Certificate) throws -> [UInt8]
}
