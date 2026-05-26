import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './components/layout/sidebar/sidebar.component';
import { ToastContainerComponent } from './components/layout/toast-container/toast-container.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, ToastContainerComponent],
  template: `
    <div class="app-container">
      <app-sidebar *ngIf="isLoggedIn()"></app-sidebar>
      <div class="main-content" [ngClass]="{'with-sidebar': isLoggedIn()}">
        <router-outlet></router-outlet>
      </div>
      <app-toast-container></app-toast-container>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      height: 100vh;
      width: 100%;
    }
    .main-content {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }
    .main-content.with-sidebar {
      margin-left: 250px;
    }
    @media (max-width: 768px) {
      .main-content.with-sidebar {
        margin-left: 0;
      }
    }
  `]
})
export class AppComponent {
  title = 'mind-guard-frontend';

  constructor(private authService: AuthService) {}

  isLoggedIn(): boolean {
    return this.authService.isAuthenticated();
  }
}
