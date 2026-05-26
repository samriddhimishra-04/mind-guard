import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {
  isCollapsed = false;
  currentUser: any = null;
  currentRoute = '';

  navItems: NavItem[] = [
    {
      label: 'Dashboard',
      icon: '📊',
      route: '/dashboard',
      roles: ['PATIENT']
    },
    {
      label: 'Journal',
      icon: '📔',
      route: '/journal',
      roles: ['PATIENT']
    },
    {
      label: 'Moods',
      icon: '😊',
      route: '/moods',
      roles: ['PATIENT']
    },
    {
      label: 'Alerts',
      icon: '🔔',
      route: '/alerts',
      roles: ['PATIENT']
    },
    {
      label: 'Profile',
      icon: '👤',
      route: '/profile',
      roles: ['PATIENT', 'THERAPIST']
    },
    {
      label: 'Therapist Dashboard',
      icon: '👨‍⚕️',
      route: '/therapist',
      roles: ['THERAPIST']
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.updateCurrentRoute();
    this.router.events.subscribe(() => {
      this.updateCurrentRoute();
    });
  }

  updateCurrentRoute(): void {
    this.currentRoute = this.router.url;
  }

  getVisibleItems(): NavItem[] {
    if (!this.currentUser) {
      return [];
    }
    return this.navItems.filter(item =>
      !item.roles || item.roles.includes(this.currentUser.role)
    );
  }

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  isActive(route: string): boolean {
    return this.currentRoute.includes(route);
  }
}
