import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TherapistService, TherapistPatientResponse, PatientAlert } from '../../services/therapist.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-therapist-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './therapist-dashboard.component.html',
  styleUrls: ['./therapist-dashboard.component.scss']
})
export class TherapistDashboardComponent implements OnInit {
  currentUser: any = null;
  activeTab: string = 'dashboard';

  patients: TherapistPatientResponse[] = [];
  alerts: PatientAlert[] = [];
  filteredAlerts: PatientAlert[] = [];

  patientsLoading = false;
  alertsLoading = false;
  patientsError: string | null = null;
  alertsError: string | null = null;

  patientCount = 0;
  criticalAlertsCount = 0;
  unresolvableAlertsCount = 0;

  patientSearch = '';
  alertStatusFilter = '';
  alertLevelFilter = '';

  constructor(
    private authService: AuthService,
    private therapistService: TherapistService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser || this.currentUser.role !== 'THERAPIST') {
      this.router.navigate(['/login']);
      return;
    }
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loadPatients();
    this.loadAlerts();
  }

  loadPatients(): void {
    this.patientsLoading = true;
    this.patientsError = null;
    this.therapistService.getPatients().subscribe({
      next: (patients) => {
        this.patients = patients;
        this.patientCount = patients.length;
        this.patientsLoading = false;
      },
      error: (error) => {
        this.patientsError = 'Failed to load patients';
        console.error('Error loading patients:', error);
        this.patientsLoading = false;
      }
    });
  }

  loadAlerts(): void {
    this.alertsLoading = true;
    this.alertsError = null;
    this.therapistService.getTherapistAlerts().subscribe({
      next: (alerts) => {
        this.alerts = alerts;
        this.filteredAlerts = alerts;
        this.criticalAlertsCount = alerts.filter(a => a.level === 'CRITICAL').length;
        this.unresolvableAlertsCount = alerts.filter(a => a.status !== 'RESOLVED').length;
        this.alertsLoading = false;
      },
      error: (error) => {
        this.alertsError = 'Failed to load alerts';
        console.error('Error loading alerts:', error);
        this.alertsLoading = false;
      }
    });
  }

  filterAlerts(): void {
    this.filteredAlerts = this.alerts.filter(alert => {
      const matchStatus = !this.alertStatusFilter || alert.status === this.alertStatusFilter;
      const matchLevel = !this.alertLevelFilter || alert.level === this.alertLevelFilter;
      return matchStatus && matchLevel;
    });
  }

  onStatusFilterChange(): void {
    this.filterAlerts();
  }

  onLevelFilterChange(): void {
    this.filterAlerts();
  }

  getFilteredPatients(): TherapistPatientResponse[] {
    if (!this.patientSearch.trim()) {
      return this.patients;
    }
    const search = this.patientSearch.toLowerCase();
    return this.patients.filter(p =>
      p.patientName.toLowerCase().includes(search) ||
      p.patientEmail.toLowerCase().includes(search)
    );
  }

  viewPatientDetail(patientId: string): void {
    this.router.navigate(['/therapist/patient', patientId]);
  }

  selectTab(tab: string): void {
    this.activeTab = tab;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getAlertColor(level: string): string {
    switch (level) {
      case 'CRITICAL':
        return '#f44336';
      case 'HIGH':
        return '#ff9800';
      case 'MEDIUM':
        return '#ffb74d';
      case 'LOW':
        return '#4caf50';
      default:
        return '#999';
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'NEW':
        return 'badge-new';
      case 'REVIEWED':
        return 'badge-reviewed';
      case 'ACKNOWLEDGED':
        return 'badge-acknowledged';
      case 'RESOLVED':
        return 'badge-resolved';
      default:
        return 'badge-default';
    }
  }

  getTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (seconds < 60) return 'just now';
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
    if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;
    return date.toLocaleDateString();
  }
}
