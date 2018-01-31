//
//  LicenseCategory.swift
//  RDWHolder
//
//  Created by mDL developer account on 17/11/2017.
//  
//

import UIKit

class LicenseCategory: NSObject {
    let licenseType: String!
    let fromDate: String!
    let toDate: String!
    
    init(mdlData: Data) {
        let parts = mdlData.split(separator: 0x3b)
        licenseType = String(bytes: parts[0], encoding: String.Encoding.ascii)
        fromDate = Util.bcdToDateString(bcd: parts[1])
        toDate = Util.bcdToDateString(bcd: parts[2])
    }
}
