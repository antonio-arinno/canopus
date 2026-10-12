import { Component, inject, OnInit, signal, WritableSignal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { Product } from '@core/model/product';
import { ProductService } from '@features/product/data/product.service';
import { ProductListViewService } from '@features/product/data/product-list-view.service';
import { Technology } from '@core/model/technology';
import { ModelMapperService } from '@core/model/model-mapper.service';
import { RequestStateService } from '@core/ui/request-state.service';

@Component({
  selector: 'app-product',
  imports: [
    MatCardModule, 
    MatTableModule, 
    MatButtonModule, 
    MatExpansionModule, 
    MatFormFieldModule, 
    MatButtonToggleModule, 
    ReactiveFormsModule,
    MatPaginatorModule
  ],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements OnInit {

  // Inyección funcional moderna con modificadores privados de solo lectura
  private readonly productService = inject(ProductService);
  private readonly productListViewService = inject(ProductListViewService);
  private readonly modelMapperService = inject(ModelMapperService);
  private readonly requestStateService = inject(RequestStateService);
  private readonly router = inject(Router);

  modoControl = new FormControl('collapsed');
  responsibleControl = new FormControl('personal');

  // Consulta tipada del acordeón nativo de Angular Material
  accordion = viewChild.required(MatAccordion);

  technologies: WritableSignal<Technology[]> = signal([]);
  products: Product[] = [];
  pageIndex = 0;
  pageSize = 10;
  totalProducts = 0;
  
  // Enlaces de estado reactivo global controlados por el errorInterceptor
  readonly isLoading = this.requestStateService.isLoading;
  readonly errorMessage = this.requestStateService.errorMessage;
  readonly emptyMessage = 'No products found.';

  displayedColumns: string[] = ['name', 'description', 'responsible', 'backup', 'time'];

  ngOnInit(): void {
    this.productList();

    // Escucha reactiva a los cambios de filtros del usuario
    this.responsibleControl.valueChanges.subscribe(() => {
      this.pageIndex = 0;
      this.productList();
    });
    
    this.modoControl.valueChanges.subscribe(mode => {
      if (mode === 'expanded') {
        this.accordion().openAll();
      } else {
        this.accordion().closeAll();
      }
    });
  }

  productList(): void {
    this.requestStateService.start();
    this.technologies.set([]);
    this.products = [];

    const scope = this.responsibleControl.value ?? 'personal';

    this.productService.getPage(this.pageIndex, this.pageSize, scope).subscribe({
      next: (response) => {
        const payload = this.modelMapperService.mapProductList(response.content as unknown[]);
        this.products = [...payload];
        this.totalProducts = response.totalElements;
        this.pageIndex = response.number;
        this.pageSize = response.size;
        this.updateList();
        this.requestStateService.finish();
      },
      error: () => this.requestStateService.finish()
    });

    this.modoControl.setValue('collapsed', { emitEvent: false });
  }

  updateList(): void {
    const techTree = this.productListViewService.buildTechnologyTree(this.products);
    this.technologies.set(techTree);
  }

  changePage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.productList();
  }

  hasItems(): boolean {
    return this.technologies().length > 0;
  }

  edit(id: number): void {
    this.router.navigate(['/pvt/product/detail', id]);
  }

  create(): void {
    this.router.navigate(['/pvt/product/detail']);  
  }
}
