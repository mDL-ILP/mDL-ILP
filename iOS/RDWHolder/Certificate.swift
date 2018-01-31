//
//  Certificate.swift
//  RDWHolder
//
//
//  
//

import UIKit

class Certificate: NSObject {
    let privateKey : SecKey
    let privateKeyString : String
    let dataGroups : [String: DataGroup]
    init(privateKey : String, dataGroups : [String : DataGroup])  throws {
        self.privateKeyString = privateKey
        self.dataGroups = dataGroups
        
        let key = CryptographyUtil.importPrivateKey(privateKeyString: privateKeyString)
        
        if (key == nil) {
                throw ParsingError.wrongPrivateKey
        }
        self.privateKey = key!
    }
}
