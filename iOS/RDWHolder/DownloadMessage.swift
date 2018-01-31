//
//  DownloadMessage.swift
//  RDWHolder
//
//
//  
//

import UIKit
import ObjectMapper
class DownloadMessage: NSObject, Mappable {
    var token: String?
    required init? (map: Map){
        
    }
    override init() {
        
    }
    func mapping(map: Map) {
        token <- map["token"]
    }
}
