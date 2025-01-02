import { NgForOf, TitleCasePipe } from '@angular/common';
import { Component } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterLink } from '@angular/router';
import { SideNavItem } from './side-nave-item';

@Component({
  selector: 'app-side-nav',
  imports: [RouterLink, MatSidenavModule, MatListModule, TitleCasePipe, NgForOf],
  templateUrl: './side-nav.component.html',
  styleUrl: './side-nav.component.scss'
})
export class SideNavComponent {

  sideNavContent: SideNavItem[] = [
    {
      title: 'dashboard',
      link: 'pvt/dashboard',
    }, 
    {
      title: 'imputation',
      link: 'pvt/imputation',
    }, 
    {
      title: 'project',
      link: 'pvt/project',
    },
    {
      title: 'product',
      link: 'pvt/product',
    },
    {
      title: 'user',
      link: 'pvt/user',
    }
  ];  

}
