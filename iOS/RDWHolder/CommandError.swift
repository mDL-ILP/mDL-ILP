//
//  CommandError.swift
//  RDWHolder
//
//
//  
//

import UIKit

enum CommandError: Error {
    case NotImplementedCommand
    case FileIdentifierNotFound( message : String)
    case NoCaseMatched(message : String)
    case Ask(message : String)
    case EncryptionError
}
