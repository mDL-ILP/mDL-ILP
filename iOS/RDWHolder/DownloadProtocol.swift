//
//  DownloadProtocol.swift
//  RDWHolder
//
//
//  
//

import UIKit

protocol DownloadProtocol: RDWErrorProtocol {
    func onSuccessfullDownload(certificate : String)
}
