//
//  DetailViewModel.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import Foundation
import SwiftData
import UIKit

@Observable
class DetailViewModel {
    var isEditing: Bool = false
    var title: String = ""
    var creator: String = ""
    var status: MediaStatus = .planned
    var rating: Double?
    var review: String = ""
    var progressCurrent: Int = 0
    var progressTotal: Int = 0
    var imageData: Data?
    
    private let item: MediaItem
    private let modelContext: ModelContext
    
    init(item: MediaItem, modelContext: ModelContext) {
        self.item = item
        self.modelContext = modelContext
        
        // Initialize form fields with current values
        title = item.title
        creator = item.creator
        status = item.status
        rating = item.rating
        review = item.review
        progressCurrent = item.progressCurrent
        progressTotal = item.progressTotal
        imageData = item.imageData
    }
    
    func saveChanges() {
        item.title = title
        item.creator = creator
        item.status = status
        item.rating = rating
        item.review = review
        item.progressCurrent = progressCurrent
        item.progressTotal = progressTotal
        item.imageData = imageData
        
        try? modelContext.save()
        isEditing = false
    }
    
    func cancelEditing() {
        // Restore original values
        title = item.title
        creator = item.creator
        status = item.status
        rating = item.rating
        review = item.review
        progressCurrent = item.progressCurrent
        progressTotal = item.progressTotal
        imageData = item.imageData
        isEditing = false
    }
    
    func deleteItem() {
        modelContext.delete(item)
        try? modelContext.save()
    }
    
    func pickImage(_ uiImage: UIImage) {
        imageData = uiImage.jpegData(compressionQuality: 0.8)
    }
    
    var progressPercentage: Double {
        guard progressTotal > 0 else { return 0 }
        return min(Double(progressCurrent) / Double(progressTotal), 1.0)
    }
    
    var availableStatuses: [MediaStatus] {
        MediaStatus.statuses(for: item.type)
    }
    
    var mediaType: MediaType {
        return item.type
    }
}
