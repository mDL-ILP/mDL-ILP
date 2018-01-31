//
//  RegistrationMessage.swift
//  RDWHolder
//
//
//  
//

import UIKit
import ObjectMapper

class RegistrationMessage: NSObject, Mappable {
    var deviceToken : String?
    var deviceDescription: String?
    var publicKey : String?
    required init? (map: Map){
        
    }
    override init() {
        
    }
    func mapping(map: Map) {
        deviceToken <- map["deviceToken"]
        deviceDescription <- map["deviceDescription"]
        publicKey <- map["publicKey"]
    }
}
