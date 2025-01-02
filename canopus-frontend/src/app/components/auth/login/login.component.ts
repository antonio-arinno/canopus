import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { User } from '@core/model/user';
import { AuthService } from '@core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [MatCardModule, ReactiveFormsModule, MatInputModule, MatButtonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit{

  formGroup!: FormGroup;
  user!: User;

  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    this.formGroup = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  submit(event: Event):void {
    this.user = new User();
    this.user = this.formGroup.value;
    this.authService.login(this.user).subscribe({
      next: (res: any) => {
        this.authService.saveUser(res.token);
        this.authService.saveToken(res.token);
        this.router.navigateByUrl('/pvt');
      },
      error: (err: any) => {
        console.log('Error: ');
        console.log(err);
      },
    });
  }



}
