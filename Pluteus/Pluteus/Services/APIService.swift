//
//  APIService.swift
//  Pluteus
//
//  Created by Developer on 2024.
//

import Foundation
import UIKit

enum APIError: LocalizedError {
    case invalidURL
    case networkError(Error)
    case decodingError(Error)
    case noResults
    case serverError(Int)
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Неверный URL запроса"
        case .networkError(let error):
            return "Ошибка сети: \(error.localizedDescription)"
        case .decodingError(let error):
            return "Ошибка обработки данных: \(error.localizedDescription)"
        case .noResults:
            return "Ничего не найдено"
        case .serverError(let code):
            return "Ошибка сервера: \(code)"
        }
    }
}

class APIService {
    static let shared = APIService()
    private let session: URLSession
    
    private init() {
        let config = URLSessionConfiguration.default
        config.requestCachePolicy = .returnCacheDataElseLoad
        config.urlCache = URLCache(memoryCapacity: 50_000_000, diskCapacity: 100_000_000)
        session = URLSession(configuration: config)
    }
    
    // MARK: - Search Movies/Series (TMDB)
    func searchMedia(query: String, type: MediaType) async throws -> [TMDBResult] {
        guard !query.isEmpty else { throw APIError.noResults }
        
        let endpoint: String
        switch type {
        case .movie:
            endpoint = "/search/movie"
        case .series:
            endpoint = "/search/tv"
        case .book:
            throw APIError.invalidURL // Для книг используем другой метод
        }
        
        var components = URLComponents(string: "\(APIConfig.tmdbBaseURL)\(endpoint)")
        components?.queryItems = [
            URLQueryItem(name: "api_key", value: APIConfig.tmdbAPIKey),
            URLQueryItem(name: "query", value: query),
            URLQueryItem(name: "language", value: "ru-RU"),
            URLQueryItem(name: "page", value: "1")
        ]
        
        guard let url = components?.url else {
            throw APIError.invalidURL
        }
        
        return try await fetch(from: url, type: TMDBSearchResponse.self).results
    }
    
    // MARK: - Search Books (Google Books)
    func searchBooks(query: String) async throws -> [GoogleBookItem] {
        guard !query.isEmpty else { throw APIError.noResults }
        
        var components = URLComponents(string: "\(APIConfig.googleBooksBaseURL)/volumes")
        components?.queryItems = [
            URLQueryItem(name: "q", value: query),
            URLQueryItem(name: "maxResults", value: "20"),
            URLQueryItem(name: "langRestrict", value: "ru"),
            URLQueryItem(name: "orderBy", value: "relevance")
        ]
        
        guard let url = components?.url else {
            throw APIError.invalidURL
        }
        
        let response = try await fetch(from: url, type: GoogleBooksResponse.self)
        return response.items ?? []
    }
    
    // MARK: - Download Image
    func downloadImage(from url: URL) async throws -> UIImage {
        let (data, response) = try await session.data(from: url)
        
        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw APIError.serverError((response as? HTTPURLResponse)?.statusCode ?? 0)
        }
        
        guard let image = UIImage(data: data) else {
            throw APIError.decodingError(NSError(domain: "ImageDecoding", code: 0, userInfo: nil))
        }
        
        return image
    }
    
    // MARK: - Generic Fetch
    private func fetch<T: Decodable>(from url: URL, type: T.Type) async throws -> T {
        let (data, response) = try await session.data(from: url)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.networkError(NSError(domain: "InvalidResponse", code: 0, userInfo: nil))
        }
        
        guard (200...299).contains(httpResponse.statusCode) else {
            throw APIError.serverError(httpResponse.statusCode)
        }
        
        do {
            let decoder = JSONDecoder()
            decoder.keyDecodingStrategy = .convertFromSnakeCase
            return try decoder.decode(T.self, from: data)
        } catch {
            throw APIError.decodingError(error)
        }
    }
}
