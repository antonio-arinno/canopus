import { NgForOf, TitleCasePipe } from '@angular/common';
import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SideNavItem } from './side-nave-item';

@Component({
  selector: 'app-side-nav',
  imports: [RouterLink, RouterLinkActive, MatSidenavModule, MatListModule, MatIconModule, TitleCasePipe, NgForOf],
  templateUrl: './side-nav.component.html',
  styleUrl: './side-nav.component.scss'
})
export class SideNavComponent {

  sideNavContent: SideNavItem[] = [
    {
      title: 'imputation',
      link: 'pvt/imputation',
      icon: 'timeline'
    },
    {
      title: 'project',
      link: 'pvt/project',
      icon: 'folder_open'
    },
    {
      title: 'product',
      link: 'pvt/product',
      icon: 'inventory_2'
    },
    {
      title: 'technology',
      link: 'pvt/technology',
      icon: 'memory'
    },
    {
      title: 'user',
      link: 'pvt/user',
      icon: 'group'
    }
  ];

}
