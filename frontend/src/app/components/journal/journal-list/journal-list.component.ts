import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { JournalService, JournalEntryResponse } from '../../../services/journal.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-journal-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './journal-list.component.html',
  styleUrls: ['./journal-list.component.scss']
})
export class JournalListComponent implements OnInit {
  entries: JournalEntryResponse[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private journalService: JournalService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEntries();
  }

  loadEntries(): void {
    this.journalService.getEntries().subscribe({
      next: (entries) => {
        this.entries = entries;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load journal entries';
        this.loading = false;
        console.error('Error loading entries:', error);
      }
    });
  }

  viewEntry(entryId: string): void {
    this.router.navigate(['/journal', entryId]);
  }

  deleteEntry(entryId: string, event: Event): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this entry?')) {
      this.journalService.deleteEntry(entryId).subscribe({
        next: () => {
          this.entries = this.entries.filter(e => e.id !== entryId);
        },
        error: (error) => {
          console.error('Error deleting entry:', error);
        }
      });
    }
  }

  getRiskLevel(distressLevel?: number): string {
    if (!distressLevel) return 'low';
    if (distressLevel > 0.7) return 'high';
    if (distressLevel > 0.4) return 'medium';
    return 'low';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}
