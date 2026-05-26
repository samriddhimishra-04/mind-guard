import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TherapistPatientResponse {
  id: string;
  therapistId: string;
  patientId: string;
  patientName: string;
  patientEmail: string;
  notes?: string;
  isActive: boolean;
  assignedAt: string;
  lastInteractionAt?: string;
  activeAlertsCount: number;
}

export interface PatientMoodLog {
  id: string;
  mood: string;
  intensityLevel?: number;
  triggers?: string;
  createdAt: string;
}

export interface PatientAlert {
  id: string;
  userId: string;
  journalEntryId: string;
  level: string;
  description: string;
  triggeredBy?: string;
  status: string;
  assignedTherapistId?: string;
  viewedAt?: string;
  resolvedAt?: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class TherapistService {
  private apiUrl = 'http://localhost:8081/api/therapists';

  constructor(private http: HttpClient) {}

  getPatients(page?: number, size?: number): Observable<TherapistPatientResponse[]> {
    const params: any = {};
    if (page !== undefined) params['page'] = page;
    if (size !== undefined) params['size'] = size;
    return this.http.get<TherapistPatientResponse[]>(`${this.apiUrl}/patients`, { params });
  }

  getPatientEntries(patientId: string, limit?: number, offset?: number): Observable<any[]> {
    const params: any = {};
    if (limit) params['limit'] = limit;
    if (offset) params['offset'] = offset;
    return this.http.get<any[]>(`${this.apiUrl}/patients/${patientId}/entries`, { params });
  }

  getPatientMoods(patientId: string, startDate?: string, endDate?: string): Observable<PatientMoodLog[]> {
    const params: any = {};
    if (startDate) params['startDate'] = startDate;
    if (endDate) params['endDate'] = endDate;
    return this.http.get<PatientMoodLog[]>(`${this.apiUrl}/patients/${patientId}/moods`, { params });
  }

  getPatientAlerts(patientId: string): Observable<PatientAlert[]> {
    return this.http.get<PatientAlert[]>(`${this.apiUrl}/alerts`, {
      params: { patientId }
    });
  }

  getTherapistAlerts(status?: string, patientId?: string): Observable<PatientAlert[]> {
    const params: any = {};
    if (status) params['status'] = status;
    if (patientId) params['patientId'] = patientId;
    return this.http.get<PatientAlert[]>(`${this.apiUrl}/alerts`, { params });
  }

  getAlert(alertId: string): Observable<PatientAlert> {
    return this.http.get<PatientAlert>(`${this.apiUrl}/alerts/${alertId}`);
  }

  assignPatient(patientId: string, notes?: string): Observable<TherapistPatientResponse> {
    const body = { notes: notes || '' };
    return this.http.post<TherapistPatientResponse>(`${this.apiUrl}/patients/${patientId}/assign`, body);
  }

  unassignPatient(patientId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/patients/${patientId}/unassign`);
  }

  resolveAlert(alertId: string, resolutionNotes?: string, recommendation?: string): Observable<PatientAlert> {
    const body = {
      resolutionNotes: resolutionNotes || '',
      recommendation: recommendation || ''
    };
    return this.http.put<PatientAlert>(`${this.apiUrl}/alerts/${alertId}/resolve`, body);
  }

  getPatientCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/patient-count`);
  }
}
