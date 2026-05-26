import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface JournalEntryRequest {
  title: string;
  content: string;
  mood?: string;
  tags?: string;
}

export interface JournalEntryResponse {
  id: string;
  title: string;
  content: string;
  mood?: string;
  sentimentScore?: number;
  distressLevel?: number;
  aiAnalysis?: string;
  isFlagged?: boolean;
  tags?: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class JournalService {
  private apiUrl = 'http://localhost:8081/api/journals';

  constructor(private http: HttpClient) {}

  createEntry(request: JournalEntryRequest): Observable<JournalEntryResponse> {
    return this.http.post<JournalEntryResponse>(`${this.apiUrl}`, request);
  }

  getEntries(): Observable<JournalEntryResponse[]> {
    return this.http.get<JournalEntryResponse[]>(`${this.apiUrl}`);
  }

  getEntry(entryId: string): Observable<JournalEntryResponse> {
    return this.http.get<JournalEntryResponse>(`${this.apiUrl}/${entryId}`);
  }

  getEntriesByDateRange(startDate: string): Observable<JournalEntryResponse[]> {
    return this.http.get<JournalEntryResponse[]>(`${this.apiUrl}/range`, {
      params: { startDate }
    });
  }

  getFlaggedEntries(): Observable<JournalEntryResponse[]> {
    return this.http.get<JournalEntryResponse[]>(`${this.apiUrl}/flagged`);
  }

  updateEntry(entryId: string, request: JournalEntryRequest): Observable<JournalEntryResponse> {
    return this.http.put<JournalEntryResponse>(`${this.apiUrl}/${entryId}`, request);
  }

  deleteEntry(entryId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${entryId}`);
  }
}
