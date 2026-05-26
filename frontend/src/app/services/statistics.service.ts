import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StatisticsResponse {
  totalEntries: number;
  totalMoodLogs: number;
  averageSentimentScore: number;
  averageDistressLevel: number;
  averageMoodIntensity: number;
  mostFrequentMood: string;
  mostFrequentTrigger: string;
  flaggedEntriesCount: number;
  moodDistribution: { [key: string]: number };
  weeklyTrend: { [key: string]: number };
}

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private apiUrl = 'http://localhost:8081/api/statistics';

  constructor(private http: HttpClient) {}

  getUserStatistics(): Observable<StatisticsResponse> {
    return this.http.get<StatisticsResponse>(`${this.apiUrl}/user`);
  }

  getPatientStatistics(patientId: string): Observable<StatisticsResponse> {
    return this.http.get<StatisticsResponse>(`${this.apiUrl}/patient/${patientId}`);
  }
}
