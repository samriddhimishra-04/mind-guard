import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface NotificationResponse {
  id: string;
  userId: string;
  type: string;
  message: string;
  relatedEntityId?: string;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8081/api/notifications';
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUnreadCount();
  }

  getNotifications(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.apiUrl}`);
  }

  getUnreadNotifications(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.apiUrl}/unread`);
  }

  getUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/unread-count`)
      .pipe(
        tap(response => this.unreadCountSubject.next(response.unreadCount))
      );
  }

  getNotificationsByType(type: string): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.apiUrl}/type/${type}`);
  }

  getNotification(id: string): Observable<NotificationResponse> {
    return this.http.get<NotificationResponse>(`${this.apiUrl}/${id}`);
  }

  markAsRead(id: string): Observable<NotificationResponse> {
    return this.http.put<NotificationResponse>(`${this.apiUrl}/${id}/read`, {})
      .pipe(
        tap(() => {
          const current = this.unreadCountSubject.value;
          if (current > 0) {
            this.unreadCountSubject.next(current - 1);
          }
        })
      );
  }

  markAllAsRead(): Observable<{ status: string; message: string }> {
    return this.http.put<{ status: string; message: string }>(`${this.apiUrl}/read-all`, {})
      .pipe(
        tap(() => this.unreadCountSubject.next(0))
      );
  }

  deleteNotification(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  private loadUnreadCount(): void {
    this.getUnreadCount().subscribe({
      next: (response) => {
        this.unreadCountSubject.next(response.unreadCount);
      },
      error: (error) => {
        console.error('Failed to load unread count:', error);
      }
    });
  }

  refreshUnreadCount(): void {
    this.loadUnreadCount();
  }
}
