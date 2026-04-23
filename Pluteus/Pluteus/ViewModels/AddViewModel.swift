//
//  AddViewModel.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import Foundation
import SwiftData
import UIKit
import SwiftUI

@Observable
class AddViewModel {
    var selectedType: MediaType = .movie
    var searchQuery: String = ""
    var searchResults: SearchResults = .empty
    var isLoading: Bool = false
    var errorMessage: String?
    
    // Form fields
    var title: String = ""
    var creator: String = ""
    var status: MediaStatus = .planned
    var rating: Double?
    var review: String = ""
    var progressCurrent: Int = 0
    var progressTotal: Int = 0
    var imageData: Data?
    var selectedImageURL: URL?
    
    enum SearchResults {
        case empty
        case loading
        case movies([TMDBResult])
        case series([TMDBResult])
        case books([GoogleBookItem])
        case error(String)
    }
    
    private let modelContext: ModelContext
    
    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }
    
    func search() async {
        guard !searchQuery.isEmpty else { return }
        
        isLoading = true
        searchResults = .loading
        errorMessage = nil
        
        do {
            switch selectedType {
            case .movie:
                let results = try await APIService.shared.searchMedia(query: searchQuery, type: .movie)
                searchResults = results.isEmpty ? .error("Ничего не найдено") : .movies(results)
            case .series:
                let results = try await APIService.shared.searchMedia(query: searchQuery, type: .series)
                searchResults = results.isEmpty ? .error("Ничего не найдено") : .series(results)
            case .book:
                let results = try await APIService.shared.searchBooks(query: searchQuery)
                searchResults = results.isEmpty ? .error("Ничего не найдено") : .books(results)
            }
        } catch {
            searchResults = .error(error.localizedDescription)
            errorMessage = error.localizedDescription
        }
        
        isLoading = false
    }
    
    func selectMovie(_ result: TMDBResult) {
        title = result.displayTitle
        creator = "" // TMDB search doesn't include director in search results
        selectedImageURL = result.posterURL
        downloadImage(from: result.posterURL)
    }
    
    func selectSeries(_ result: TMDBResult) {
        title = result.displayTitle
        creator = ""
        selectedImageURL = result.posterURL
        downloadImage(from: result.posterURL)
    }
    
    func selectBook(_ result: GoogleBookItem) {
        title = result.volumeInfo.title
        creator = result.volumeInfo.authorName
        progressTotal = result.volumeInfo.pageCount ?? 0
        selectedImageURL = result.volumeInfo.coverURL
        downloadImage(from: result.volumeInfo.coverURL)
    }
    
    private func downloadImage(from url: URL?) {
        guard let url = url else { return }
        
        Task {
            do {
                let image = try await APIService.shared.downloadImage(from: url)
                imageData = image.jpegData(compressionQuality: 0.8)
            } catch {
                print("Error downloading image: \(error)")
            }
        }
    }
    
    func saveItem() {
        guard !title.isEmpty else { return }
        
        let item = MediaItem(
            type: selectedType,
            title: title,
            creator: creator,
            status: status,
            rating: rating,
            review: review,
            progressCurrent: progressCurrent,
            progressTotal: progressTotal,
            imageData: imageData
        )
        
        modelContext.insert(item)
        
        do {
            try modelContext.save()
        } catch {
            print("Error saving item: \(error)")
        }
    }
    
    func pickImage(_ uiImage: UIImage) {
        imageData = uiImage.jpegData(compressionQuality: 0.8)
        selectedImageURL = nil
    }
    
    func resetForm() {
        selectedType = .movie
        searchQuery = ""
        searchResults = .empty
        isLoading = false
        errorMessage = nil
        title = ""
        creator = ""
        status = .planned
        rating = nil
        review = ""
        progressCurrent = 0
        progressTotal = 0
        imageData = nil
        selectedImageURL = nil
    }
    
    var availableStatuses: [MediaStatus] {
        MediaStatus.statuses(for: selectedType)
    }
}
