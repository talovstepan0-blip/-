//
//  ContentViewModel.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import Foundation
import SwiftData
import SwiftUI

@Observable
class ContentViewModel {
    var mediaItems: [MediaItem] = []
    var filteredItems: [MediaItem] = []
    
    var selectedTypeFilter: MediaType? = nil
    var selectedStatusFilter: MediaStatus? = nil
    var searchQuery: String = ""
    var sortOption: SortOption = .dateAdded
    
    enum SortOption: String, CaseIterable {
        case dateAdded = "Дата добавления"
        case title = "Название"
        case rating = "Оценка"
        
        var ascending: Bool {
            switch self {
            case .title: return true
            case .rating: return false
            case .dateAdded: return false
            }
        }
    }
    
    private let modelContext: ModelContext
    
    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        fetchItems()
    }
    
    func fetchItems() {
        let descriptor = FetchDescriptor<MediaItem>()
        do {
            mediaItems = try modelContext.fetch(descriptor)
            applyFiltersAndSort()
        } catch {
            print("Error fetching items: \(error)")
        }
    }
    
    func applyFiltersAndSort() {
        filteredItems = mediaItems.filter { item in
            // Фильтр по типу
            if let typeFilter = selectedTypeFilter, item.type != typeFilter {
                return false
            }
            
            // Фильтр по статусу
            if let statusFilter = selectedStatusFilter, item.status != statusFilter {
                return false
            }
            
            // Поиск по названию
            if !searchQuery.isEmpty {
                return item.title.localizedCaseInsensitiveContains(searchQuery)
            }
            
            return true
        }
        
        // Сортировка
        switch sortOption {
        case .dateAdded:
            filteredItems.sort { $0.dateAdded > $1.dateAdded }
        case .title:
            filteredItems.sort { $0.title < $1.title }
        case .rating:
            filteredItems.sort { ($0.rating ?? 0) > ($1.rating ?? 0) }
        }
    }
    
    func deleteItem(_ item: MediaItem) {
        modelContext.delete(item)
        fetchItems()
    }
    
    func updateStatus(_ item: MediaItem, to status: MediaStatus) {
        item.status = status
        try? modelContext.save()
        fetchItems()
    }
}
