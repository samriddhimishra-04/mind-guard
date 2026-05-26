import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration: number;
  dismissible: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toasts$ = new BehaviorSubject<Toast[]>([]);
  public toasts: Observable<Toast[]> = this.toasts$.asObservable();

  private toastId = 0;

  constructor() {}

  success(message: string, duration: number = 3000): void {
    this.addToast(message, 'success', duration, true);
  }

  error(message: string, duration: number = 5000): void {
    this.addToast(message, 'error', duration, true);
  }

  warning(message: string, duration: number = 4000): void {
    this.addToast(message, 'warning', duration, true);
  }

  info(message: string, duration: number = 3000): void {
    this.addToast(message, 'info', duration, true);
  }

  private addToast(message: string, type: 'success' | 'error' | 'warning' | 'info', duration: number, dismissible: boolean): void {
    const id = `toast-${++this.toastId}`;
    const toast: Toast = { id, message, type, duration, dismissible };

    const currentToasts = this.toasts$.value;
    this.toasts$.next([...currentToasts, toast]);

    if (duration > 0) {
      setTimeout(() => this.removeToast(id), duration);
    }
  }

  removeToast(id: string): void {
    const currentToasts = this.toasts$.value;
    this.toasts$.next(currentToasts.filter(t => t.id !== id));
  }

  clear(): void {
    this.toasts$.next([]);
  }
}
