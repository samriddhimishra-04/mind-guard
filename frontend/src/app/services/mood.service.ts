import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MoodLogRequest {
  mood: string;
  notes?: string;
  intensityLevel?: number;
  triggers?: string;
}

export interface MoodLogResponse {
  id: string;
  mood: string;
  notes?: string;
  intensityLevel?: number;
  triggers?: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MoodService {
  private apiUrl = 'http://localhost:8081/api/moods';

  constructor(private http: HttpClient) {}

  logMood(request: MoodLogRequest): Observable<MoodLogResponse> {
    return this.http.post<MoodLogResponse>(`${this.apiUrl}`, request);
  }

  getMoodLogs(): Observable<MoodLogResponse[]> {
    return this.http.get<MoodLogResponse[]>(`${this.apiUrl}`);
  }

  getMoodLog(moodLogId: string): Observable<MoodLogResponse> {
    return this.http.get<MoodLogResponse>(`${this.apiUrl}/${moodLogId}`);
  }

  getMoodLogsByDateRange(startDate: string): Observable<MoodLogResponse[]> {
    return this.http.get<MoodLogResponse[]>(`${this.apiUrl}/range`, {
      params: { startDate }
    });
  }

  getMoodLogsByType(mood: string): Observable<MoodLogResponse[]> {
    return this.http.get<MoodLogResponse[]>(`${this.apiUrl}/type/${mood}`);
  }

  getMoodLogsByExactRange(startDate: string, endDate: string): Observable<MoodLogResponse[]> {
    return this.http.get<MoodLogResponse[]>(`${this.apiUrl}/range/exact`, {
      params: { startDate, endDate }
    });
  }

  deleteMoodLog(moodLogId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${moodLogId}`);
  }
}
