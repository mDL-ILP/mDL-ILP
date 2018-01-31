//
//  RequestTokenMessage.swift
//  RDWHolder
//
//  Created by mDL developer account on 03/11/2017.
//  
//

import UIKit
import ObjectMapper
class RequestTokenMessage: NSObject, Mappable {
    var deviceToken: String?
    var permittedDatagroups: Array<String>?
    
    required init? (map: Map){
        
    }
    override init() {
        
    }
    func mapping(map: Map) {
        deviceToken <- map["deviceToken"]
        permittedDatagroups <- map["permittedDatagroups"]
    }
}
