import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MoodService, MoodLogResponse } from '../../../services/mood.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-mood-tracker',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mood-tracker.component.html',
  styleUrls: ['./mood-tracker.component.scss']
})
export class MoodTrackerComponent implements OnInit {
  moodForm: FormGroup;
  moodLogs: MoodLogResponse[] = [];
  loading = false;
  submitted = false;
  error: string | null = null;

  moodOptions = ['Happy', 'Sad', 'Anxious', 'Calm', 'Energetic', 'Tired', 'Confused', 'Stressed'];

  constructor(
    private formBuilder: FormBuilder,
    private moodService: MoodService
  ) {
    this.moodForm = this.formBuilder.group({
      mood: ['', Validators.required],
      notes: ['', Validators.maxLength(500)],
      intensityLevel: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      triggers: ['', Validators.maxLength(500)]
    });
  }

  ngOnInit(): void {
    this.loadMoodLogs();
  }

  get f() {
    return this.moodForm.controls;
  }

  loadMoodLogs(): void {
    console.log('Loading mood logs...');
    this.moodService.getMoodLogs().subscribe({
      next: (logs) => {
        console.log('Mood logs loaded:', logs);
        this.moodLogs = logs;
      },
      error: (error) => {
        console.error('Error loading mood logs:', error);
        console.error('Status:', error?.status);
        console.error('Message:', error?.message);
        console.error('Error details:', error?.error);
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = null;

    if (this.moodForm.invalid) {
      return;
    }

    this.loading = true;
    const formData = {
      ...this.moodForm.value,
      mood: this.moodForm.value.mood.toUpperCase()
    };

    this.moodService.logMood(formData).subscribe({
      next: (response) => {
        console.log('Mood logged successfully:', response);
        this.moodLogs.unshift(response);
        this.moodForm.reset({ intensityLevel: 5 });
        this.submitted = false;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error logging mood:', error);
        console.error('Status:', error?.status);
        console.error('Error message:', error?.error?.message);
        this.error = error?.error?.message || error?.message || 'Failed to log mood';
        this.loading = false;
      }
    });
  }

  deleteMoodLog(id: string, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this mood log?')) {
      this.moodService.deleteMoodLog(id).subscribe({
        next: () => {
          this.moodLogs = this.moodLogs.filter(log => log.id !== id);
        },
        error: (error) => {
          console.error('Error deleting mood log:', error);
        }
      });
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
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

  getIntensityColor(intensity?: number): string {
    if (!intensity) return '#ffb74d';
    if (intensity >= 8) return '#f44336';
    if (intensity >= 6) return '#ff9800';
    if (intensity >= 4) return '#ffb74d';
    return '#4caf50';
  }
}
