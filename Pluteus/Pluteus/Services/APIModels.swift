//
//  APIModels.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import Foundation

// MARK: - TMDB Models (Movies & Series)
struct TMDBSearchResponse: Codable {
    let results: [TMDBResult]
}

struct TMDBResult: Codable {
    let id: Int
    let title: String?
    let name: String?
    let overview: String
    let posterPath: String?
    let releaseDate: String?
    let firstAirDate: String?
    
    var displayTitle: String {
        return title ?? name ?? "Unknown"
    }
    
    var posterURL: URL? {
        guard let path = posterPath else { return nil }
        return URL(string: "https://image.tmdb.org/t/p/w500\(path)")
    }
}

struct TMDBMovieDetail: Codable {
    let id: Int
    let title: String
    let overview: String
    let posterPath: String?
    let releaseDate: String?
    let runtime: Int?
    let genres: [Genre]?
    
    struct Genre: Codable {
        let name: String
    }
}

struct TMSeriesDetail: Codable {
    let id: Int
    let name: String
    let overview: String
    let posterPath: String?
    let firstAirDate: String?
    let numberOfSeasons: Int?
    let numberOfEpisodes: Int?
    let createdBy: [Creator]?
    
    struct Creator: Codable {
        let name: String
    }
}

// MARK: - Google Books Models
struct GoogleBooksResponse: Codable {
    let items: [GoogleBookItem]?
}

struct GoogleBookItem: Codable {
    let id: String
    let volumeInfo: VolumeInfo
}

struct VolumeInfo: Codable {
    let title: String
    let authors: [String]?
    let description: String?
    let imageLinks: ImageLinks?
    let publishedDate: String?
    let pageCount: Int?
    
    var authorName: String {
        return authors?.first ?? "Неизвестный автор"
    }
    
    var coverURL: URL? {
        guard let smallThumbnail = imageLinks?.smallThumbnail else { return nil }
        // Replace http with https if needed and use larger image
        var urlString = smallThumbnail.replacingOccurrences(of: "http:", with: "https:")
        urlString = urlString.replacingOccurrences(of: "&zoom=1", with: "&zoom=2")
        return URL(string: urlString)
    }
}

struct ImageLinks: Codable {
    let smallThumbnail: String
    let thumbnail: String?
}

// MARK: - API Configuration
enum APIConfig {
    static let tmdbAPIKey = "YOUR_TMDB_API_KEY" // Замените на ваш ключ
    static let googleBooksBaseURL = "https://www.googleapis.com/books/v1"
    static let tmdbBaseURL = "https://api.themoviedb.org/3"
}
