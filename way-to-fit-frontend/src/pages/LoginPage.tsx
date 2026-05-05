import { Button } from "@/components/ui/button";
import { Dumbbell } from "lucide-react";

export function LoginPage() {
  const handleGoogleLogin = () => {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace('/api', '') || 'http://localhost:8080';
    const redirectUri = `${window.location.origin}/auth/callback`;
    window.location.href = `${apiBaseUrl}/oauth2/authorization/google?redirect_uri=${redirectUri}`;
  };

  return (
    <div className="flex min-h-screen w-full bg-background">
      {/* Left section: Branding & Graphics */}
      <div className="relative hidden w-0 flex-1 flex-col justify-center bg-zinc-900 lg:flex dark:bg-zinc-950 overflow-hidden">
        {/* Abstract background decorative shapes */}
        <div className="absolute inset-0 bg-gradient-to-br from-primary/20 via-zinc-900 to-zinc-900" />
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-primary/30 blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[60%] h-[60%] rounded-full bg-primary/20 blur-[150px]" />
        
        <div className="relative z-10 px-12 lg:px-24 xl:px-32 flex flex-col items-start space-y-8 animate-in fade-in slide-in-from-left-8 duration-1000">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary shadow-lg shadow-primary/30">
              <Dumbbell className="h-6 w-6 text-primary-foreground" />
            </div>
            <span className="text-2xl font-bold tracking-tight text-white">
              Way To Fit
            </span>
          </div>
          
          <div className="space-y-4">
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-tight">
              완벽한 <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-blue-600">크로스핏 박스</span> <br/>
              관리의 시작.
            </h1>
            <p className="max-w-xl text-lg text-zinc-400 font-medium break-keep">
              크로스핏 대표님을 위해 설계된 올인원 B2B 플랫폼입니다. 
              회원 관리부터 결제, 수업 스케줄링까지 모두 하나의 매끄러운 환경에서 처리하세요.
            </p>
          </div>
          
          <div className="flex items-center gap-4 pt-8">
            <div className="flex flex-col border-l-2 border-primary/50 pl-4">
              <span className="text-3xl font-bold text-white">100+</span>
              <span className="text-sm font-medium text-zinc-400">도입 박스</span>
            </div>
            <div className="flex flex-col border-l-2 border-primary/50 pl-4 ml-6">
              <span className="text-3xl font-bold text-white">99.9%</span>
              <span className="text-sm font-medium text-zinc-400">안정성</span>
            </div>
          </div>
        </div>
      </div>

      {/* Right section: Login Form */}
      <div className="flex flex-col justify-center flex-1 px-4 py-12 sm:px-6 lg:flex-none lg:w-[480px] xl:w-[560px] 2xl:w-[640px] bg-card">
        <div className="w-full max-w-sm mx-auto animate-in fade-in slide-in-from-right-8 duration-700 delay-150 fill-mode-both">
          {/* Mobile logo */}
          <div className="flex items-center gap-3 lg:hidden mb-12">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary shadow-md">
              <Dumbbell className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold tracking-tight text-foreground">
              Way To Fit
            </span>
          </div>

          <div>
            <h2 className="text-3xl font-bold tracking-tight text-foreground">
              환영합니다
            </h2>
            <p className="mt-2 text-sm text-muted-foreground break-keep">
              박스 관리를 위해 로그인을 진행해 주세요.
            </p>
          </div>

          <div className="mt-8">
            <div className="space-y-6">
              <Button
                variant="outline"
                type="button"
                className="w-full h-12 text-md font-medium border-border hover:bg-secondary/50 transition-colors shadow-sm"
                onClick={handleGoogleLogin}
              >
                <img
                  src="https://www.svgrepo.com/show/475656/google-color.svg"
                  className="w-5 h-5 mr-3"
                  alt="Google logo"
                />
                Google 계정으로 계속하기
              </Button>
            </div>

            <div className="mt-8 relative">
              <div
                className="absolute inset-0 flex items-center"
                aria-hidden="true"
              >
                <div className="w-full border-t border-border" />
              </div>
              <div className="relative flex justify-center text-sm font-medium leading-6">
                <span className="bg-card px-4 text-muted-foreground">
                  소셜 간편 로그인
                </span>
              </div>
            </div>

            <div className="mt-8">
              <p className="text-center text-sm text-muted-foreground">
                소셜 계정으로 계속하기를 누르시면 당사의{' '}
                <a href="#" className="font-semibold text-primary hover:text-primary/80 transition-colors">
                  이용약관
                </a>
                {' '}및{' '}
                <a href="#" className="font-semibold text-primary hover:text-primary/80 transition-colors">
                  개인정보 처리방침
                </a>
                에 동의하게 됩니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
