//
//  ConfigurationManager.swift
//  RDWHolder
//
//
//  
//

import UIKit

class ConfigurationManager: NSObject {
    private static let typeOfConfiguration : String = Bundle.main.object(forInfoDictionaryKey: "Config")! as! String
    private static let path: String = Bundle.main.path(forResource: "\(typeOfConfiguration)-configuration", ofType: "plist")!
    private static let sampleConf: NSDictionary = NSDictionary(contentsOfFile: path)!
    public static let RDW_ENDPOINT_BASE = load(key: "rdw_endpoint_base")
    public static let RDW_REGISTER = load(key: "rdw_register")
    public static let RDW_REQUEST_TRANSFER_ID = load(key : "rdw_request_transfer_id")
    public static let RDW_PERMIT_TRANSFER = load(key : "rdw_permit_transfer")
    public static let RDW_DOWNLOAD = load(key: "rdw_download")
    public static let RDW_REQUEST_TOKEN = load(key: "rdw_request_token")
    public static let SHOW_PRINT = load(key: "show_print")
    private static func load(key : String) -> String {
        return sampleConf.object(forKey: key) as! String
    }
}
