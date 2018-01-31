//
//  FileUtil.swift
//  RDWHolder
//
//
//  
//

import UIKit

class FileUtil: NSObject {
    private static let fileName = "license"
    private static let fileExtension = "ul"
    public static func saveLicense < T : FileManagerProtocol>(delegate : T, license: String) {
        if let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first {
            
            let dir = try? FileManager.default.url(for: .documentDirectory,
                                                   in: .userDomainMask, appropriateFor: nil, create: true)
            
            // If the directory was found, we write a file to it and read it back
            if let fileURL = dir?.appendingPathComponent(fileName).appendingPathExtension(fileExtension) {
                
                Util.printValue("fileURL = \(fileURL)")
                do {
                    try license.write(to: fileURL, atomically: true, encoding: .utf8)
                    
                    delegate.onSuccessfullySavedLicense()
                } catch {
                    let error = "Failed writing to URL: \(fileURL), Error: " + error.localizedDescription
                    print("\(error)")
                    delegate.onError(message: error)
                }
            }else{
                let error = "file url not found!"
                print(error)
                delegate.onError(message: error)
            }
        }else{
            let error = "director not found!"
            print(error)
            delegate.onError(message: error)
        }
    }
    public static func readLicense<T : FileManagerProtocol> (delegate : T) -> String? {
        // get the documents folder url
        let documentDirectory = try! FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        // create the destination url for the text file to be saved
        let fileURL = documentDirectory.appendingPathComponent("\(fileName).\(fileExtension)")
        
            // reading from disk
            do {
                let mytext = try String(contentsOf: fileURL)
                return mytext
            } catch {
                let message = "error loading contents of:  \(fileURL), \(error)"
                Util.printValue(message)
                delegate.onError(message: message)
                return nil
            }
            
    }
    /*public static func readLicense <T : FileManagerProtocol> (delegate : T) -> String? {
        if let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first {
            
            let dir = try? FileManager.default.url(for: .documentDirectory,
                                                   in: .userDomainMask, appropriateFor: nil, create: true)
            if let fileURL = dir?.appendingPathComponent(fileName).appendingPathExtension(fileExtension) {
                var inString = ""
                do {
                    inString = try String(contentsOf: fileURL)
                    return inString
                } catch {
                    let error = "Failed reading from URL: \(fileURL), Error: " + error.localizedDescription
                    print("\(error)")
                    delegate.onError(message: error)
                    return nil
                }
            }else {
                let error = "file url not found!"
                print(error)
                delegate.onError(message: error)
                return nil
            }
        }else{
            let error = "director not found!"
            print(error)
            delegate.onError(message: error)
            return nil
        }
    }*/
    public static func deleteLicense <T : FileManagerProtocol > (delegate : T) {
        let fileManager = FileManager.default
        let dir = try? FileManager.default.url(for: .documentDirectory,
                                               in: .userDomainMask, appropriateFor: nil, create: true)
        if let fileURL = dir?.appendingPathComponent(fileName).appendingPathExtension(fileExtension) {
            do {
                try fileManager.removeItem(atPath: fileURL.path)
                UserDefaults.standard.removeObject(forKey: SharedApplicationConstants.typeOfCertificate)
                delegate.onSuccessfullyDeletedLicense()
            }
            catch let error as NSError {
                let message = "Error: \(error)"
                print(message)
                delegate.onError(message: message)
            }
        }else{
            let error = "director not found!"
            print(error)
            delegate.onError(message: error)
        }
    }
}




