import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-header',
  imports: [MatToolbarModule, MatIconModule, MatMenuModule, MatButtonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {

  authService = inject(AuthService);
  router = inject(Router);

  logout(): void{
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

}
