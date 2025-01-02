import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';

import { HeaderComponent } from './header/header.component';
import { SideNavComponent } from './side-nav/side-nav.component';
import { FooterComponent } from "../footer/footer.component";

@Component({
  selector: 'app-private',
  imports: [RouterOutlet, HeaderComponent, MatSidenavModule, SideNavComponent, FooterComponent],
  templateUrl: './private.component.html',
  styleUrl: './private.component.scss'
})
export class PrivateComponent {

}
