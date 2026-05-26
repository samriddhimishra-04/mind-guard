import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { PatientDashboardComponent } from './components/patient-dashboard/patient-dashboard.component';
import { TherapistDashboardComponent } from './components/therapist-dashboard/therapist-dashboard.component';
import { PatientDetailComponent } from './components/therapist-dashboard/patient-detail/patient-detail.component';
import { JournalListComponent } from './components/journal/journal-list/journal-list.component';
import { MoodTrackerComponent } from './components/mood/mood-tracker/mood-tracker.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'dashboard',
    component: PatientDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'journal',
    component: JournalListComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'moods',
    component: MoodTrackerComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'statistics',
    component: JournalListComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'alerts',
    component: JournalListComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'profile',
    component: JournalListComponent,
    canActivate: [AuthGuard],
    data: { roles: ['PATIENT'] }
  },
  {
    path: 'therapist',
    component: TherapistDashboardComponent,
    canActivate: [AuthGuard],
    data: { roles: ['THERAPIST'] }
  },
  {
    path: 'therapist/patient/:patientId',
    component: PatientDetailComponent,
    canActivate: [AuthGuard],
    data: { roles: ['THERAPIST'] }
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  }
];
