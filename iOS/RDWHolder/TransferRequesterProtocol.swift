//
//  TransferRequesterProtocol.swift
//  RDWHolder
//
//
//  
//

import UIKit

protocol TransferRequesterProtocol : RDWErrorProtocol {
    func onTransferRequestIdReceived(id : String)
}
