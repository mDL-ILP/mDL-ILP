//
//  CertificateLoader.swift
//  RDWHolder
//
//
//  
//

import UIKit

protocol CertificateLoader {
    
    func loadCertificate(certificate : String) -> Certificate?
    
}
