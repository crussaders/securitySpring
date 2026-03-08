import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('isLoggedIn should return false when no token', () => {
    localStorage.removeItem('auth_token');
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('logout should remove token', () => {
    localStorage.setItem('auth_token', 'test-token');
    service.logout();
    expect(service.getToken()).toBeNull();
  });
});
