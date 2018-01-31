//
//  GeneralError.swift
//  RDWHolder
//
//
//  
//

import UIKit

enum ParsingError: Error {
    case invalidRequest (message : String)
    case wrongPrivateKey
}
