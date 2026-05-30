import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ArrowRight, Dumbbell } from "lucide-react";

export function LoginPage() {
  const handleGoogleLogin = () => {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.replace('/api', '') || 'http://localhost:8080';
    const redirectUri = `${window.location.origin}/auth/callback`;
    window.location.href = `${apiBaseUrl}/oauth2/authorization/google?redirect_uri=${redirectUri}`;
  };

  return (
    <div className="min-h-screen bg-background">
      <div className="flex min-h-screen flex-col lg:flex-row">
        <section className="flex flex-1 border-b border-border bg-primary/5 lg:border-b-0 lg:border-r lg:border-r-primary/10">
          <div className="mx-auto flex w-full max-w-[800px] items-center px-6 py-12 sm:px-10 lg:px-16">
            <div className="max-w-xl space-y-8">
              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary text-primary-foreground">
                  <Dumbbell className="h-5 w-5" />
                </div>
                <div>
                  <p className="text-lg font-semibold text-foreground">Way To Fit</p>
                </div>
              </div>

              <div className="space-y-3">
                <h1 className="text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">
                  대회 운영,
                  <br />
                  한곳에서.
                </h1>
                <p className="text-sm leading-6 text-muted-foreground">
                  생성, 승인, 판독, 리더보드
                </p>
              </div>
            </div>
          </div>
        </section>

        <section className="flex w-full items-center justify-center px-4 py-10 sm:px-6 lg:w-[440px] lg:px-10 xl:w-[480px]">
          <Card className="w-full max-w-md rounded-xl border-border shadow-sm">
            <CardContent className="p-6 sm:p-8">
              <div className="space-y-6">
                <div className="space-y-3">
                  <div>
                    <h2 className="text-3xl font-semibold tracking-tight text-foreground">
                      로그인
                    </h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                      운영 권한이 있는 계정으로 접속합니다.
                    </p>
                  </div>
                </div>

                <Button
                  type="button"
                  className="h-12 w-full justify-center gap-3 text-sm font-semibold"
                  onClick={handleGoogleLogin}
                >
                  <img
                    src="https://www.svgrepo.com/show/475656/google-color.svg"
                    className="h-5 w-5"
                    alt="Google logo"
                  />
                  Google 계정으로 로그인
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </div>
            </CardContent>
          </Card>
        </section>
      </div>
    </div>
  );
}
