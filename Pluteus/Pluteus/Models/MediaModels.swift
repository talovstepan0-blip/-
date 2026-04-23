//
//  Models.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import Foundation
import SwiftData

enum MediaType: String, CaseIterable, Codable {
    case movie = "Фильм"
    case series = "Сериал"
    case book = "Книга"
    
    var displayName: String {
        return self.rawValue
    }
}

enum MediaStatus: String, CaseIterable, Codable {
    // Фильмы и сериалы
    case planned = "Планирую"
    case watching = "Смотрю"
    case completed = "Просмотрено"
    
    // Книги (переопределяем отображение в коде при необходимости)
    case reading = "Читаю"
    case read = "Прочитано"
    
    var displayName: String {
        return self.rawValue
    }
    
    static func statuses(for type: MediaType) -> [MediaStatus] {
        switch type {
        case .movie, .series:
            return [.planned, .watching, .completed]
        case .book:
            return [.planned, .reading, .read]
        }
    }
    
    var displayTitle: String {
        switch self {
        case .planned:
            return "Планирую"
        case .watching, .reading:
            return type(of: self) == MediaStatus.self ? (self == .watching ? "Смотрю" : "Читаю") : "В процессе"
        case .completed, .read:
            return self == .completed ? "Просмотрено" : "Прочитано"
        }
    }
}

@Model
final class MediaItem {
    var id: UUID
    var type: MediaType
    var title: String
    var creator: String // Режиссёр или Автор
    var status: MediaStatus
    var rating: Double? // Оценка от 0 до 10
    var review: String
    var dateAdded: Date
    var imageData: Data?
    
    // Прогресс
    var progressCurrent: Int // просмотренные серии или прочитанные страницы
    var progressTotal: Int   // всего серий или страниц
    
    init(type: MediaType, 
         title: String, 
         creator: String = "", 
         status: MediaStatus = .planned,
         rating: Double? = nil,
         review: String = "",
         progressCurrent: Int = 0,
         progressTotal: Int = 0,
         imageData: Data? = nil) {
        
        self.id = UUID()
        self.type = type
        self.title = title
        self.creator = creator
        self.status = status
        self.rating = rating
        self.review = review
        self.dateAdded = Date()
        self.progressCurrent = progressCurrent
        self.progressTotal = progressTotal
        self.imageData = imageData
    }
    
    var progressPercentage: Double {
        guard progressTotal > 0 else { return 0 }
        return min(Double(progressCurrent) / Double(progressTotal), 1.0)
    }
    
    var isCompleted: Bool {
        if type == .movie {
            return status == .completed
        } else {
            return progressCurrent >= progressTotal && progressTotal > 0
        }
    }
}
