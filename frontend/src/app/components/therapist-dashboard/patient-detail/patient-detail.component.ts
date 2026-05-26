import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { TherapistService, TherapistPatientResponse, PatientAlert } from '../../../services/therapist.service';
import { JournalService, JournalEntryResponse } from '../../../services/journal.service';
import { MoodService, MoodLogResponse } from '../../../services/mood.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './patient-detail.component.html',
  styleUrls: ['./patient-detail.component.scss']
})
export class PatientDetailComponent implements OnInit {
  patientId: string = '';
  patient: TherapistPatientResponse | null = null;
  activeTab: string = 'overview';

  journalEntries: JournalEntryResponse[] = [];
  moodLogs: MoodLogResponse[] = [];
  patientAlerts: PatientAlert[] = [];

  loadingPatient = false;
  loadingEntries = false;
  loadingMoods = false;
  loadingAlerts = false;

  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private therapistService: TherapistService,
    private journalService: JournalService,
    private moodService: MoodService
  ) {}

  ngOnInit(): void {
    this.patientId = this.route.snapshot.paramMap.get('patientId') || '';
    if (!this.patientId) {
      this.router.navigate(['/therapist']);
      return;
    }
    this.loadPatientData();
  }

  loadPatientData(): void {
    this.loadPatient();
    this.loadEntries();
    this.loadMoods();
    this.loadAlerts();
  }

  loadPatient(): void {
    this.loadingPatient = true;
    this.therapistService.getPatients().subscribe({
      next: (patients) => {
        this.patient = patients.find(p => p.patientId === this.patientId) || null;
        this.loadingPatient = false;
      },
      error: (error) => {
        this.error = 'Failed to load patient information';
        this.loadingPatient = false;
      }
    });
  }

  loadEntries(): void {
    this.loadingEntries = true;
    this.therapistService.getPatientEntries(this.patientId, 10, 0).subscribe({
      next: (entries) => {
        this.journalEntries = entries;
        this.loadingEntries = false;
      },
      error: (error) => {
        console.error('Failed to load entries:', error);
        this.loadingEntries = false;
      }
    });
  }

  loadMoods(): void {
    this.loadingMoods = true;
    this.therapistService.getPatientMoods(this.patientId).subscribe({
      next: (moods) => {
        this.moodLogs = moods;
        this.loadingMoods = false;
      },
      error: (error) => {
        console.error('Failed to load moods:', error);
        this.loadingMoods = false;
      }
    });
  }

  loadAlerts(): void {
    this.loadingAlerts = true;
    this.therapistService.getPatientAlerts(this.patientId).subscribe({
      next: (alerts) => {
        this.patientAlerts = alerts;
        this.loadingAlerts = false;
      },
      error: (error) => {
        console.error('Failed to load alerts:', error);
        this.loadingAlerts = false;
      }
    });
  }

  selectTab(tab: string): void {
    this.activeTab = tab;
  }

  unassignPatient(): void {
    if (confirm('Are you sure you want to unassign this patient?')) {
      this.therapistService.unassignPatient(this.patientId).subscribe({
        next: () => {
          this.router.navigate(['/therapist']);
        },
        error: (error) => {
          alert('Failed to unassign patient');
        }
      });
    }
  }

  resolveAlert(alertId: string): void {
    const resolutionNotes = prompt('Enter resolution notes:');
    if (resolutionNotes !== null) {
      this.therapistService.resolveAlert(alertId, resolutionNotes).subscribe({
        next: () => {
          this.loadAlerts();
        },
        error: (error) => {
          alert('Failed to resolve alert');
        }
      });
    }
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

  getStatusColor(status: string): string {
    switch (status) {
      case 'NEW':
        return '#1976d2';
      case 'REVIEWED':
        return '#7b1fa2';
      case 'ACKNOWLEDGED':
        return '#e65100';
      case 'RESOLVED':
        return '#2e7d32';
      default:
        return '#999';
    }
  }

  getMoodEmoji(mood: string): string {
    const emojiMap: { [key: string]: string } = {
      'Happy': '😊',
      'Sad': '😢',
      'Anxious': '😰',
      'Calm': '😌',
      'Energetic': '🤩',
      'Tired': '😴',
      'Confused': '😕',
      'Stressed': '😤'
    };
    return emojiMap[mood] || '😐';
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

  goBack(): void {
    this.router.navigate(['/therapist']);
  }

  getUnresolvedAlerts(): PatientAlert[] {
    return this.patientAlerts.filter(a => a.status !== 'RESOLVED');
  }

  getCriticalAlerts(): PatientAlert[] {
    return this.patientAlerts.filter(a => a.level === 'CRITICAL' && a.status !== 'RESOLVED');
  }
}
