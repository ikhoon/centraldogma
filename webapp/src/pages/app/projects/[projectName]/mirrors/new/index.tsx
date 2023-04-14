import {Controller, useForm} from "react-hook-form";
import {MirrorDto} from "dogma/features/mirror/MirrorDto";
import {useAddNewMirrorMutation, useGetReposQuery} from "dogma/features/api/apiSlice";
import {useAppDispatch} from "dogma/store";
import {FetchBaseQueryError} from "@reduxjs/toolkit/query";
import {SerializedError} from "@reduxjs/toolkit";
import {createMessage} from "dogma/features/message/messageSlice";
import Router, {useRouter} from "next/router";
import ErrorHandler from "dogma/features/services/ErrorHandler";
import {
  Alert,
  AlertDescription,
  AlertIcon,
  AlertTitle,
  Button,
  Center,
  FormControl,
  FormErrorMessage,
  FormHelperText,
  FormLabel,
  Heading,
  Icon,
  Input,
  Link,
  Radio,
  RadioGroup,
  Spacer,
  Stack,
  Textarea,
  VStack
} from "@chakra-ui/react";
import {Select} from "chakra-react-select";
import {GoArrowBoth, GoArrowDown, GoArrowUp, GoKey, GoRepo} from "react-icons/go";
import {BiTimer} from "react-icons/bi";
import {IconType} from "react-icons";
import {ExternalLinkIcon} from "@chakra-ui/icons";
import {IoBanSharp} from "react-icons/io5";
import {GiMirrorMirror} from "react-icons/gi";

interface RepoOptionType {
  value: string;
  label: string;
}

const MIRROR_SCHEMES = ['git+http', 'git+https', 'git+ssh']
  .map((scheme) => ({value: scheme, label: scheme}));

const LabelledIcon = ({icon, text}: { icon: IconType, text: string }) => (
  <><Icon as={icon} boxSize={4} marginBottom="-3px"/> {text}</>
);

const NewMirrorPage = () => {
  const router = useRouter();
  const projectName = router.query.projectName ? (router.query.projectName as string) : '';

  const {
    register,
    handleSubmit,
    reset,
    formState: {errors}, control,
  } = useForm<MirrorDto>();

  const {data: repos, error} = useGetReposQuery(projectName);
  const repoOptions: RepoOptionType[] = (repos || [])
    .map((repo) => ({
      value: repo.name,
      label: repo.name,
    }));
  const [addNewMirror, {isLoading}] = useAddNewMirrorMutation();

  const dispatch = useAppDispatch();
  const onSubmit = async (formData: MirrorDto) => {
    console.log(formData);
    return;
    // TODO(ikhoon): Implement
    try {
      const response = await addNewMirror(formData).unwrap();
      if ((response as { error: FetchBaseQueryError | SerializedError }).error) {
        throw (response as { error: FetchBaseQueryError | SerializedError }).error;
      }
      dispatch(
        createMessage({
          title: 'New mirror is created',
          text: `Successfully created`,
          type: 'success',
        }),
      );
      reset();
      Router.push(`/app/projects/${projectName}/`);
    } catch (error) {
      dispatch(
        createMessage({
          title: `Failed to create a new mirror`,
          text: ErrorHandler.handle(error),
          type: 'error',
        }),
      );
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Center>
        <VStack width='80%' align='left'>
          <Heading align='center'>New mirror for <b>{projectName}</b> project</Heading>
          <FormControl isRequired>
            <FormLabel><LabelledIcon icon={GiMirrorMirror} text='Name' /></FormLabel>
            <Input
              id="name"
              name="name"
              type="text"
              placeholder="The mirror name"
              {...register('name', {required: true})}
            />
            {errors.name && <FormErrorMessage>Please enter the mirror name</FormErrorMessage>}
          </FormControl>
          <Spacer />

          <FormControl isRequired>
            <FormLabel><LabelledIcon icon={BiTimer} text='Schedule'/>
            </FormLabel>
            <Input
              id="schedule"
              name="schedule"
              type="text"
              placeholder="0 * * * * *"
              value="0 * * * * *"
              {...register('schedule', {required: true})}
            />
            <FormHelperText>
              <Link color='teal.500'
                    href="https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                    isExternal>Quartz cron expression <ExternalLinkIcon mx='2px'/> </Link>
              is used to describe when the mirroring task is supposed to be triggered.
            </FormHelperText>
            {errors.name && <FormErrorMessage>Please enter the mirror name</FormErrorMessage>}
          </FormControl>
          <Spacer />

          <FormControl isRequired>
            <FormLabel><LabelledIcon icon={GoArrowBoth} text='Direction'/></FormLabel>
            <Controller
              name="direction"
              rules={{required: true}}
              control={control}
              render={({field: {onChange, value}}) => (
                <RadioGroup onChange={onChange} value={value} mb={4}>
                  <Stack direction="row">
                    <Radio value="REMOTE_TO_LOCAL" marginRight={2}>
                      <LabelledIcon icon={GoArrowDown} text='Remote to Central Dogma'/>
                    </Radio>
                    <Radio value="LOCAL_TO_REMOTE">
                      <LabelledIcon icon={GoArrowUp} text='Central Dogma to Remote'/>
                    </Radio>
                  </Stack>
                </RadioGroup>
              )}
            />
          </FormControl>

          {repoOptions.length >= 0 ? (
              <Stack direction='row' width='100%'>
                <FormControl isRequired>
                  <FormLabel><LabelledIcon icon={GoRepo} text={'Local repository'}/></FormLabel>
                  <Controller
                    control={control}
                    name="localRepo"
                    rules={{required: true}}
                    render={({field: {onChange, value, name, ref}}) => (
                      <Select
                        ref={ref}
                        id="localRepo"
                        name={name}
                        options={repoOptions}
                        // The default value of React Select must be null (and not undefined)
                        value={repoOptions?.find((option) => option.value === value) || null}
                        onChange={(option) => option && onChange(option.value)}
                        placeholder="Enter repo name..."
                        closeMenuOnSelect={true}
                        openMenuOnFocus={true}
                        isSearchable={true}
                        isClearable={true}
                      />
                    )}
                  />
                  {errors.localRepo && <FormErrorMessage>Please enter the local repo name</FormErrorMessage>}
                </FormControl>
                <FormControl isRequired width='50%'>
                  <FormLabel>path</FormLabel>
                  <Input
                    id="localPath"
                    name="localPath"
                    type="text"
                    placeholder="/"
                    {...register('localPath', {required: true})}
                  />
                </FormControl>
              </Stack>
            ) :
            <Alert status='error'>
              <AlertIcon/>
              <AlertTitle>No local repository is found.</AlertTitle>
              <AlertDescription>You need to create a local repository first.</AlertDescription>
            </Alert>
          }
          <Spacer />

          <Stack direction='row' width='100%'>
            <FormControl isRequired width='50%'>
              <FormLabel><LabelledIcon icon={GoRepo} text={'Remote repository'}/></FormLabel>
              <Controller
                control={control}
                name="localRepo"
                rules={{required: true}}
                render={({field: {onChange, value, name, ref}}) => (
                  <Select
                    ref={ref}
                    id="remoteScheme"
                    name={name}
                    options={MIRROR_SCHEMES}
                    // The default value of React Select must be null (and not undefined)
                    value={MIRROR_SCHEMES?.find((option) => option.value === value) || null}
                    onChange={(option) => option && onChange(option.value)}
                    placeholder="Git scheme"
                    closeMenuOnSelect={true}
                    openMenuOnFocus={true}
                    isSearchable={true}
                    isClearable={true}
                  />
                )}
              />
            </FormControl>
            <FormControl isRequired>
              <FormLabel>repo</FormLabel>
              <Input
                id="remoteUrl"
                name="remoteUrl"
                type="text"
                placeholder="my.git.com/org/myrepo"
                {...register('localPath', {required: true})}
              />
              {errors.name && <FormErrorMessage>Please enter the mirror name</FormErrorMessage>}
            </FormControl>
            <FormControl isRequired width='50%'>
              <FormLabel>path</FormLabel>
              <Input
                id="remotePath"
                name="remotePath"
                type="text"
                placeholder="/"
                {...register('remotePath', {required: true})}
              />
              {errors.name && <FormErrorMessage>Please enter the mirror name</FormErrorMessage>}
            </FormControl>
          </Stack>
          <Spacer/>

          {/* TODO Get credential info from... */}
          <FormControl width='50%' alignItems='left'>
            <FormLabel><LabelledIcon icon={GoKey} text={'Credential'}/></FormLabel>
            <Controller
              control={control}
              name="credentialId"
              rules={{required: true}}
              render={({field: {onChange, value, name, ref}}) => (
                <Select
                  ref={ref}
                  id="credentialId"
                  name={name}
                  options={MIRROR_SCHEMES}
                  // The default value of React Select must be null (and not undefined)
                  value={MIRROR_SCHEMES?.find((option) => option.value === value) || null}
                  onChange={(option) => option && onChange(option.value)}
                  placeholder="Enter credential ID ..."
                  closeMenuOnSelect={true}
                  openMenuOnFocus={true}
                  isSearchable={true}
                  isClearable={true}
                />
              )}
            />
          </FormControl>
          <Spacer/>

          <FormControl>
            <FormLabel><LabelledIcon icon={IoBanSharp} text={'gitignore'}/></FormLabel>
            <Textarea
              id="gitignore"
              name="gitignore"
              placeholder=""
              {...register('name', {required: true})}
            />
            <FormHelperText>
              <Link color='teal.500' href='https://git-scm.com/docs/gitignore'
                    isExternal>gitignore <ExternalLinkIcon mx='2px'/>
              </Link> that should be excluded from mirroring.
            </FormHelperText>
          </FormControl>
          <Spacer/>
          <Button type="submit" colorScheme="blue" isLoading={isLoading} loadingText="Creating">
            Create a new mirror
          </Button>
        </VStack>
      </Center>
    </form>
  );
}

export default NewMirrorPage;
