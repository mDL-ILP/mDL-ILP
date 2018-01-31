//
//  RequestTokenProtocol.swift
//  RDWHolder
//
//  Created by mDL developer account on 03/11/2017.
//  
//

import UIKit

protocol RequestTokenProtocol : RDWErrorProtocol {
    func onRequestTokenReceived(token : String)
}
