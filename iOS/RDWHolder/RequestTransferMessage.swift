//
//  RequestTransferMessage.swift
//  RDWHolder
//
//
//  
//

import UIKit
import ObjectMapper
class RequestTransferMessage: NSObject, Mappable {
    var token: String?
    required init? (map: Map){
        
    }
    override init() {
        
    }
    func mapping(map: Map) {
        token <- map["token"]
    }
}
