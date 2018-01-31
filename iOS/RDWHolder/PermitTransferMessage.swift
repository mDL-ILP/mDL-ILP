//
//  PermitTransferMessage.swift
//  RDWHolder
//
//  Created by mDL developer account on 27/11/2017.
//  
//

import UIKit
import ObjectMapper
class PermitTransferMessage: NSObject, Mappable {
    var deviceToken: String?
    var transferId: String?
    
    required init? (map: Map){
        
    }
    override init() {
        
    }
    func mapping(map: Map) {
        deviceToken <- map["deviceToken"]
        transferId <- map["transferId"]
    }
}
