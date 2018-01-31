//
//  DataGroup.swift
//  RDWHolder
//
//
//  
//

import UIKit

class DataGroup: NSObject {
    let idString : String
    let id : [UInt8]
    let name : String
    let value : [UInt8]
    
    init(idString : String, id: [UInt8], name: String, value : [UInt8]) {
        self.idString = idString
        self.id = id
        self.name = name
        self.value = value
    }
    
    func dataArray() -> Data {
        return Data(bytes: value);
    }
}
